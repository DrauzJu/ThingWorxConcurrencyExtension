import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class ConcurrencyFunctions {

	// -- Only one Mutex instance for the whole system and thing, event if
	// ThingsRestart
	// -- we don't kill mutex as maybe there's processes executing on the
	// background.
	// -- This will create zombie mutex if Things are deleted,
	// -- on server restart they will be freed, we may need to
	// -- implement a garbage collector... But deleting things it's
	// -- not a normal behaviour on ThingWorx.
	private static final ConcurrentHashMap<String, ReentrantLock> _instanceMtx = new ConcurrentHashMap<>();

	private static AtomicInteger activeLocks = new AtomicInteger(0);
	private static AtomicInteger activeWaiting = new AtomicInteger(0);

	private static void incrementLocks() {
		while (true) {
			int existingValue = ConcurrencyFunctions.activeLocks.get();
			int newValue = existingValue + 1;
			if (ConcurrencyFunctions.activeLocks.compareAndSet(existingValue, newValue)) {
				return;
			}
		}
	}

	private static void decrementLocks() {
		while (true) {
			int existingValue = ConcurrencyFunctions.activeLocks.get();
			int newValue = existingValue - 1;
			if (ConcurrencyFunctions.activeLocks.compareAndSet(existingValue, newValue)) {
				return;
			}
		}
	}

	private static void incrementWaiting() {
		while (true) {
			int existingValue = ConcurrencyFunctions.activeWaiting.get();
			int newValue = existingValue + 1;
			if (ConcurrencyFunctions.activeWaiting.compareAndSet(existingValue, newValue)) {
				return;
			}
		}
	}

	private static void decrementWaiting() {
		while (true) {
			int existingValue = ConcurrencyFunctions.activeWaiting.get();
			int newValue = existingValue - 1;
			if (ConcurrencyFunctions.activeWaiting.compareAndSet(existingValue, newValue)) {
				return;
			}
		}
	}

	private static ReentrantLock getMutexById(String id) throws Exception {
		ReentrantLock meMtx = ConcurrencyFunctions._instanceMtx.get(id);
		if (meMtx == null) {
			meMtx = ConcurrencyFunctions._instanceMtx.computeIfAbsent(id, k -> new ReentrantLock(true));
		}
		return meMtx;
	}

	public static long Concurrency_GetTotalActiveLocks(org.mozilla.javascript.Context cx,
			org.mozilla.javascript.Scriptable thisObj, Object[] args, org.mozilla.javascript.Function funObj) {
		return ConcurrencyFunctions.activeLocks.get();
	}

	public static long Concurrency_GetTotalActiveWaiting(org.mozilla.javascript.Context cx,
			org.mozilla.javascript.Scriptable thisObj, Object[] args, org.mozilla.javascript.Function funObj) {
		return ConcurrencyFunctions.activeWaiting.get();
	}

	public static long Concurrency_GetTotalThingsLocksUsage(org.mozilla.javascript.Context cx,
			org.mozilla.javascript.Scriptable thisObj, Object[] args, org.mozilla.javascript.Function funObj) {
		return ConcurrencyFunctions._instanceMtx.size();
	}

	public static void Concurrency_Lock(org.mozilla.javascript.Context cx, org.mozilla.javascript.Scriptable thisObj,
			Object[] args, org.mozilla.javascript.Function funObj) throws Exception {
		final ReentrantLock mutex = ConcurrencyFunctions.getMutexById(args[0].toString());
		if (mutex != null) {
			ConcurrencyFunctions.incrementWaiting();
			mutex.lock();
			// -- we must ensure that the lock it's returned, otherwise we must unlock here.
			try {
				ConcurrencyFunctions.decrementWaiting();
				ConcurrencyFunctions.incrementLocks();
			} catch (Exception e) {
				mutex.unlock();
				throw new Exception(
						"Lock_ConcurrencyFunctions/Failed to to additional steps, waiting counter maybe corrupted.");
			}
		} else {
			throw new Exception("Lock_ConcurrencyFunctions/Cannot get instance Mutex");
		}
	}

	public static Boolean Concurrency_TryLock(org.mozilla.javascript.Context cx,
			org.mozilla.javascript.Scriptable thisObj, Object[] args, org.mozilla.javascript.Function funObj)
			throws Exception {

		Long timeOut = Long.valueOf(-1);
		if (args.length > 1) {
			timeOut = Long.parseLong(args[1].toString());
		}

		final ReentrantLock mutex = ConcurrencyFunctions.getMutexById(args[0].toString());
		if (mutex != null) {
			final Boolean result;
			Boolean incremented = false;
			if (((long) timeOut) < 0) {
				result = mutex.tryLock();
			} else {
				incremented = true;
				ConcurrencyFunctions.incrementWaiting();
				result = mutex.tryLock((long) timeOut, TimeUnit.MILLISECONDS);
			}

			if (result == true) {
				// -- we must ensure that the lock it's returned, otherwise we must unlock here.
				try {
					if (incremented == true)
						ConcurrencyFunctions.decrementWaiting();
					ConcurrencyFunctions.incrementLocks();
				} catch (Exception e) {
					mutex.unlock();
					throw new Exception(
							"TryLock_ConcurrencyFunctions/Failed to do additional steps, waiting counter maybe corrupted.");
				}
			}
			return result;
		} else {
			throw new Exception("TryLock_ConcurrencyFunctions/Cannot get instance Mutex");
		}
	}

	public static void Concurrency_Unlock(org.mozilla.javascript.Context cx, org.mozilla.javascript.Scriptable thisObj,
			Object[] args, org.mozilla.javascript.Function funObj) throws Exception {
		final ReentrantLock mutex = ConcurrencyFunctions.getMutexById(args[0].toString());
		if (mutex != null) {
			mutex.unlock();
			ConcurrencyFunctions.decrementLocks();
		} else {
			throw new Exception("Unlock_ConcurrencyFunctions/Cannot get instance Mutex");
		}
	}

	public static Boolean Concurrency_IsLocked(org.mozilla.javascript.Context cx,
			org.mozilla.javascript.Scriptable thisObj, Object[] args, org.mozilla.javascript.Function funObj)
			throws Exception {
		final ReentrantLock mutex = ConcurrencyFunctions.getMutexById(args[0].toString());
		if (mutex != null) {
			return mutex.isLocked();
		}
		return false;
	}

}
