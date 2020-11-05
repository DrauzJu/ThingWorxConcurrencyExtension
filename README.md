Simplified version of https://github.com/carlescm/ThingWorxConcurrencyExtension

# ThingWorx Concurrency Extension

The aim of this extension it's to provide the Swiss Tool for Concurrency in ThingWorx.

## Contents

- [How It Works](#how-it-works)
- [Compatibility](#compatibility)
- [Installation](#installation)
- [Usage](#usage)
- [Build](#build)
- [Acknowledgments](#acknowledgments)
- [Author](#author)


## How It Works

It publishes standard Java concurrency features in order to be used easily on ThingWorx Server Side Javascript. Also, it may try to solve typical concurrency problems like doing an autoincrement counter.

## Compatibility

ThingWorx 7.3 and above. It's set to minimum ThingWorx 6.5 and built with ThingWorx 6.5 SDK but I didn't tested on it.

## Installation

Import the extension (ConcurrencyExtension.zip) with ThingWorx Composer Import Extension feature.

## Usage: Concurrency Script Functions

List of script helper functions related with this concurrency extension. This services should go on a subsystem like entity, but subsystems on ThingWorx can't be created through extensions :(

### Concurrency_GetTotalActiveLocks

Returns the total active locks, in the whole ThingWorx running system.

### Concurrency_GetTotalActiveWaiting

Returns the total active threads which are waiting on a lock, in the whole ThingWorx running system.

### Concurrency_GetTotalThingsLocksUsage

Returns the total number of mutex created on Things (ReentranLocks), in the whole ThingWorx running system since last start.

### Concurrency_Lock

Creates/Reuses a mutex with the given id and blocks execution until it gets the mutex.

### Concurrency_Unlock

Unlocks a mutex with the given id.

### Concurrency_TryLock

Creates/Reuses a mutex with the given id and blocks execution until it gets the mutex or the given timeout happens.

### Concurrency_IsLocked

Checks if the mutex with the given id it's locked right now.

## Build

If you need to build it, it's built with gradle and java 11. Version change it's done by hand and should be automated.

## Acknowledgments

I've started from the [code](https://community.ptc.com/t5/ThingWorx-Developers/Concurrency-Synchronisation-ConcurrentModificationException/m-p/624921) posted by [@antondorf](https://community.ptc.com/t5/user/viewprofilepage/user-id/290654) on the ThingWorx Developer Community.


## Author

[Carles Coll Madrenas](https://linkedin.com/in/carlescoll)
