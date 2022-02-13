# Low level design - ConnectionPool

## Problem Statement
We have to desing and implement a connectionPool

## Business(Functional) requirment
*  User should be able to create a pool of given max size.
* At any point of time, maximum number of connections in the pool should not exceed max size.
* Follow lazy initialisation principle, while creating pool
