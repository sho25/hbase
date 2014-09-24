begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|util
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Deque
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|LinkedList
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|classification
operator|.
name|InterfaceAudience
import|;
end_import

begin_comment
comment|/**  * Computes the optimal (minimal cost) assignment of jobs to workers (or other  * analogous) concepts given a cost matrix of each pair of job and worker, using  * the algorithm by James Munkres in "Algorithms for the Assignment and  * Transportation Problems", with additional optimizations as described by Jin  * Kue Wong in "A New Implementation of an Algorithm for the Optimal Assignment  * Problem: An Improved Version of Munkres' Algorithm". The algorithm runs in  * O(n^3) time and need O(n^2) auxiliary space where n is the number of jobs or  * workers, whichever is greater.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|MunkresAssignment
block|{
comment|// The original algorithm by Munkres uses the terms STAR and PRIME to denote
comment|// different states of zero values in the cost matrix. These values are
comment|// represented as byte constants instead of enums to save space in the mask
comment|// matrix by a factor of 4n^2 where n is the size of the problem.
specifier|private
specifier|static
specifier|final
name|byte
name|NONE
init|=
literal|0
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
name|STAR
init|=
literal|1
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
name|PRIME
init|=
literal|2
decl_stmt|;
comment|// The algorithm requires that the number of column is at least as great as
comment|// the number of rows. If that is not the case, then the cost matrix should
comment|// be transposed before computation, and the solution matrix transposed before
comment|// returning to the caller.
specifier|private
specifier|final
name|boolean
name|transposed
decl_stmt|;
comment|// The number of rows of internal matrices.
specifier|private
specifier|final
name|int
name|rows
decl_stmt|;
comment|// The number of columns of internal matrices.
specifier|private
specifier|final
name|int
name|cols
decl_stmt|;
comment|// The cost matrix, the cost of assigning each row index to column index.
specifier|private
name|float
index|[]
index|[]
name|cost
decl_stmt|;
comment|// Mask of zero cost assignment states.
specifier|private
name|byte
index|[]
index|[]
name|mask
decl_stmt|;
comment|// Covering some rows of the cost matrix.
specifier|private
name|boolean
index|[]
name|rowsCovered
decl_stmt|;
comment|// Covering some columns of the cost matrix.
specifier|private
name|boolean
index|[]
name|colsCovered
decl_stmt|;
comment|// The alternating path between starred zeroes and primed zeroes
specifier|private
name|Deque
argument_list|<
name|Pair
argument_list|<
name|Integer
argument_list|,
name|Integer
argument_list|>
argument_list|>
name|path
decl_stmt|;
comment|// The solution, marking which rows should be assigned to which columns. The
comment|// positions of elements in this array correspond to the rows of the cost
comment|// matrix, and the value of each element correspond to the columns of the cost
comment|// matrix, i.e. assignments[i] = j indicates that row i should be assigned to
comment|// column j.
specifier|private
name|int
index|[]
name|assignments
decl_stmt|;
comment|// Improvements described by Jin Kue Wong cache the least value in each row,
comment|// as well as the column index of the least value in each row, and the pending
comment|// adjustments to each row and each column.
specifier|private
name|float
index|[]
name|leastInRow
decl_stmt|;
specifier|private
name|int
index|[]
name|leastInRowIndex
decl_stmt|;
specifier|private
name|float
index|[]
name|rowAdjust
decl_stmt|;
specifier|private
name|float
index|[]
name|colAdjust
decl_stmt|;
comment|/**    * Construct a new problem instance with the specified cost matrix. The cost    * matrix must be rectangular, though not necessarily square. If one dimension    * is greater than the other, some elements in the greater dimension will not    * be assigned. The input cost matrix will not be modified.    * @param costMatrix    */
specifier|public
name|MunkresAssignment
parameter_list|(
name|float
index|[]
index|[]
name|costMatrix
parameter_list|)
block|{
comment|// The algorithm assumes that the number of columns is at least as great as
comment|// the number of rows. If this is not the case of the input matrix, then
comment|// all internal structures must be transposed relative to the input.
name|this
operator|.
name|transposed
operator|=
name|costMatrix
operator|.
name|length
operator|>
name|costMatrix
index|[
literal|0
index|]
operator|.
name|length
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|transposed
condition|)
block|{
name|this
operator|.
name|rows
operator|=
name|costMatrix
index|[
literal|0
index|]
operator|.
name|length
expr_stmt|;
name|this
operator|.
name|cols
operator|=
name|costMatrix
operator|.
name|length
expr_stmt|;
block|}
else|else
block|{
name|this
operator|.
name|rows
operator|=
name|costMatrix
operator|.
name|length
expr_stmt|;
name|this
operator|.
name|cols
operator|=
name|costMatrix
index|[
literal|0
index|]
operator|.
name|length
expr_stmt|;
block|}
name|cost
operator|=
operator|new
name|float
index|[
name|rows
index|]
index|[
name|cols
index|]
expr_stmt|;
name|mask
operator|=
operator|new
name|byte
index|[
name|rows
index|]
index|[
name|cols
index|]
expr_stmt|;
name|rowsCovered
operator|=
operator|new
name|boolean
index|[
name|rows
index|]
expr_stmt|;
name|colsCovered
operator|=
operator|new
name|boolean
index|[
name|cols
index|]
expr_stmt|;
name|path
operator|=
operator|new
name|LinkedList
argument_list|<
name|Pair
argument_list|<
name|Integer
argument_list|,
name|Integer
argument_list|>
argument_list|>
argument_list|()
expr_stmt|;
name|leastInRow
operator|=
operator|new
name|float
index|[
name|rows
index|]
expr_stmt|;
name|leastInRowIndex
operator|=
operator|new
name|int
index|[
name|rows
index|]
expr_stmt|;
name|rowAdjust
operator|=
operator|new
name|float
index|[
name|rows
index|]
expr_stmt|;
name|colAdjust
operator|=
operator|new
name|float
index|[
name|cols
index|]
expr_stmt|;
name|assignments
operator|=
literal|null
expr_stmt|;
comment|// Copy cost matrix.
if|if
condition|(
name|transposed
condition|)
block|{
for|for
control|(
name|int
name|r
init|=
literal|0
init|;
name|r
operator|<
name|rows
condition|;
name|r
operator|++
control|)
block|{
for|for
control|(
name|int
name|c
init|=
literal|0
init|;
name|c
operator|<
name|cols
condition|;
name|c
operator|++
control|)
block|{
name|cost
index|[
name|r
index|]
index|[
name|c
index|]
operator|=
name|costMatrix
index|[
name|c
index|]
index|[
name|r
index|]
expr_stmt|;
block|}
block|}
block|}
else|else
block|{
for|for
control|(
name|int
name|r
init|=
literal|0
init|;
name|r
operator|<
name|rows
condition|;
name|r
operator|++
control|)
block|{
name|System
operator|.
name|arraycopy
argument_list|(
name|costMatrix
index|[
name|r
index|]
argument_list|,
literal|0
argument_list|,
name|cost
index|[
name|r
index|]
argument_list|,
literal|0
argument_list|,
name|cols
argument_list|)
expr_stmt|;
block|}
block|}
comment|// Costs must be finite otherwise the matrix can get into a bad state where
comment|// no progress can be made. If your use case depends on a distinction
comment|// between costs of MAX_VALUE and POSITIVE_INFINITY, you're doing it wrong.
for|for
control|(
name|int
name|r
init|=
literal|0
init|;
name|r
operator|<
name|rows
condition|;
name|r
operator|++
control|)
block|{
for|for
control|(
name|int
name|c
init|=
literal|0
init|;
name|c
operator|<
name|cols
condition|;
name|c
operator|++
control|)
block|{
if|if
condition|(
name|cost
index|[
name|r
index|]
index|[
name|c
index|]
operator|==
name|Float
operator|.
name|POSITIVE_INFINITY
condition|)
block|{
name|cost
index|[
name|r
index|]
index|[
name|c
index|]
operator|=
name|Float
operator|.
name|MAX_VALUE
expr_stmt|;
block|}
block|}
block|}
block|}
comment|/**    * Get the optimal assignments. The returned array will have the same number    * of elements as the number of elements as the number of rows in the input    * cost matrix. Each element will indicate which column should be assigned to    * that row or -1 if no column should be assigned, i.e. if result[i] = j then    * row i should be assigned to column j. Subsequent invocations of this method    * will simply return the same object without additional computation.    * @return an array with the optimal assignments    */
specifier|public
name|int
index|[]
name|solve
parameter_list|()
block|{
comment|// If this assignment problem has already been solved, return the known
comment|// solution
if|if
condition|(
name|assignments
operator|!=
literal|null
condition|)
block|{
return|return
name|assignments
return|;
block|}
name|preliminaries
argument_list|()
expr_stmt|;
comment|// Find the optimal assignments.
while|while
condition|(
operator|!
name|testIsDone
argument_list|()
condition|)
block|{
while|while
condition|(
operator|!
name|stepOne
argument_list|()
condition|)
block|{
name|stepThree
argument_list|()
expr_stmt|;
block|}
name|stepTwo
argument_list|()
expr_stmt|;
block|}
comment|// Extract the assignments from the mask matrix.
if|if
condition|(
name|transposed
condition|)
block|{
name|assignments
operator|=
operator|new
name|int
index|[
name|cols
index|]
expr_stmt|;
name|outer
label|:
for|for
control|(
name|int
name|c
init|=
literal|0
init|;
name|c
operator|<
name|cols
condition|;
name|c
operator|++
control|)
block|{
for|for
control|(
name|int
name|r
init|=
literal|0
init|;
name|r
operator|<
name|rows
condition|;
name|r
operator|++
control|)
block|{
if|if
condition|(
name|mask
index|[
name|r
index|]
index|[
name|c
index|]
operator|==
name|STAR
condition|)
block|{
name|assignments
index|[
name|c
index|]
operator|=
name|r
expr_stmt|;
continue|continue
name|outer
continue|;
block|}
block|}
comment|// There is no assignment for this row of the input/output.
name|assignments
index|[
name|c
index|]
operator|=
operator|-
literal|1
expr_stmt|;
block|}
block|}
else|else
block|{
name|assignments
operator|=
operator|new
name|int
index|[
name|rows
index|]
expr_stmt|;
name|outer
label|:
for|for
control|(
name|int
name|r
init|=
literal|0
init|;
name|r
operator|<
name|rows
condition|;
name|r
operator|++
control|)
block|{
for|for
control|(
name|int
name|c
init|=
literal|0
init|;
name|c
operator|<
name|cols
condition|;
name|c
operator|++
control|)
block|{
if|if
condition|(
name|mask
index|[
name|r
index|]
index|[
name|c
index|]
operator|==
name|STAR
condition|)
block|{
name|assignments
index|[
name|r
index|]
operator|=
name|c
expr_stmt|;
continue|continue
name|outer
continue|;
block|}
block|}
block|}
block|}
comment|// Once the solution has been computed, there is no need to keep any of the
comment|// other internal structures. Clear all unnecessary internal references so
comment|// the garbage collector may reclaim that memory.
name|cost
operator|=
literal|null
expr_stmt|;
name|mask
operator|=
literal|null
expr_stmt|;
name|rowsCovered
operator|=
literal|null
expr_stmt|;
name|colsCovered
operator|=
literal|null
expr_stmt|;
name|path
operator|=
literal|null
expr_stmt|;
name|leastInRow
operator|=
literal|null
expr_stmt|;
name|leastInRowIndex
operator|=
literal|null
expr_stmt|;
name|rowAdjust
operator|=
literal|null
expr_stmt|;
name|colAdjust
operator|=
literal|null
expr_stmt|;
return|return
name|assignments
return|;
block|}
comment|/**    * Corresponds to the "preliminaries" step of the original algorithm.    * Guarantees that the matrix is an equivalent non-negative matrix with at    * least one zero in each row.    */
specifier|private
name|void
name|preliminaries
parameter_list|()
block|{
for|for
control|(
name|int
name|r
init|=
literal|0
init|;
name|r
operator|<
name|rows
condition|;
name|r
operator|++
control|)
block|{
comment|// Find the minimum cost of each row.
name|float
name|min
init|=
name|Float
operator|.
name|POSITIVE_INFINITY
decl_stmt|;
for|for
control|(
name|int
name|c
init|=
literal|0
init|;
name|c
operator|<
name|cols
condition|;
name|c
operator|++
control|)
block|{
name|min
operator|=
name|Math
operator|.
name|min
argument_list|(
name|min
argument_list|,
name|cost
index|[
name|r
index|]
index|[
name|c
index|]
argument_list|)
expr_stmt|;
block|}
comment|// Subtract that minimum cost from each element in the row.
for|for
control|(
name|int
name|c
init|=
literal|0
init|;
name|c
operator|<
name|cols
condition|;
name|c
operator|++
control|)
block|{
name|cost
index|[
name|r
index|]
index|[
name|c
index|]
operator|-=
name|min
expr_stmt|;
comment|// If the element is now zero and there are no zeroes in the same row
comment|// or column which are already starred, then star this one. There
comment|// must be at least one zero because of subtracting the min cost.
if|if
condition|(
name|cost
index|[
name|r
index|]
index|[
name|c
index|]
operator|==
literal|0
operator|&&
operator|!
name|rowsCovered
index|[
name|r
index|]
operator|&&
operator|!
name|colsCovered
index|[
name|c
index|]
condition|)
block|{
name|mask
index|[
name|r
index|]
index|[
name|c
index|]
operator|=
name|STAR
expr_stmt|;
comment|// Cover this row and column so that no other zeroes in them can be
comment|// starred.
name|rowsCovered
index|[
name|r
index|]
operator|=
literal|true
expr_stmt|;
name|colsCovered
index|[
name|c
index|]
operator|=
literal|true
expr_stmt|;
block|}
block|}
block|}
comment|// Clear the covered rows and columns.
name|Arrays
operator|.
name|fill
argument_list|(
name|rowsCovered
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|Arrays
operator|.
name|fill
argument_list|(
name|colsCovered
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test whether the algorithm is done, i.e. we have the optimal assignment.    * This occurs when there is exactly one starred zero in each row.    * @return true if the algorithm is done    */
specifier|private
name|boolean
name|testIsDone
parameter_list|()
block|{
comment|// Cover all columns containing a starred zero. There can be at most one
comment|// starred zero per column. Therefore, a covered column has an optimal
comment|// assignment.
for|for
control|(
name|int
name|r
init|=
literal|0
init|;
name|r
operator|<
name|rows
condition|;
name|r
operator|++
control|)
block|{
for|for
control|(
name|int
name|c
init|=
literal|0
init|;
name|c
operator|<
name|cols
condition|;
name|c
operator|++
control|)
block|{
if|if
condition|(
name|mask
index|[
name|r
index|]
index|[
name|c
index|]
operator|==
name|STAR
condition|)
block|{
name|colsCovered
index|[
name|c
index|]
operator|=
literal|true
expr_stmt|;
block|}
block|}
block|}
comment|// Count the total number of covered columns.
name|int
name|coveredCols
init|=
literal|0
decl_stmt|;
for|for
control|(
name|int
name|c
init|=
literal|0
init|;
name|c
operator|<
name|cols
condition|;
name|c
operator|++
control|)
block|{
name|coveredCols
operator|+=
name|colsCovered
index|[
name|c
index|]
condition|?
literal|1
else|:
literal|0
expr_stmt|;
block|}
comment|// Apply an row and column adjustments that are pending.
for|for
control|(
name|int
name|r
init|=
literal|0
init|;
name|r
operator|<
name|rows
condition|;
name|r
operator|++
control|)
block|{
for|for
control|(
name|int
name|c
init|=
literal|0
init|;
name|c
operator|<
name|cols
condition|;
name|c
operator|++
control|)
block|{
name|cost
index|[
name|r
index|]
index|[
name|c
index|]
operator|+=
name|rowAdjust
index|[
name|r
index|]
expr_stmt|;
name|cost
index|[
name|r
index|]
index|[
name|c
index|]
operator|+=
name|colAdjust
index|[
name|c
index|]
expr_stmt|;
block|}
block|}
comment|// Clear the pending row and column adjustments.
name|Arrays
operator|.
name|fill
argument_list|(
name|rowAdjust
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|Arrays
operator|.
name|fill
argument_list|(
name|colAdjust
argument_list|,
literal|0
argument_list|)
expr_stmt|;
comment|// The covers on columns and rows may have been reset, recompute the least
comment|// value for each row.
for|for
control|(
name|int
name|r
init|=
literal|0
init|;
name|r
operator|<
name|rows
condition|;
name|r
operator|++
control|)
block|{
name|leastInRow
index|[
name|r
index|]
operator|=
name|Float
operator|.
name|POSITIVE_INFINITY
expr_stmt|;
for|for
control|(
name|int
name|c
init|=
literal|0
init|;
name|c
operator|<
name|cols
condition|;
name|c
operator|++
control|)
block|{
if|if
condition|(
operator|!
name|rowsCovered
index|[
name|r
index|]
operator|&&
operator|!
name|colsCovered
index|[
name|c
index|]
operator|&&
name|cost
index|[
name|r
index|]
index|[
name|c
index|]
operator|<
name|leastInRow
index|[
name|r
index|]
condition|)
block|{
name|leastInRow
index|[
name|r
index|]
operator|=
name|cost
index|[
name|r
index|]
index|[
name|c
index|]
expr_stmt|;
name|leastInRowIndex
index|[
name|r
index|]
operator|=
name|c
expr_stmt|;
block|}
block|}
block|}
comment|// If all columns are covered, then we are done. Since there may be more
comment|// columns than rows, we are also done if the number of covered columns is
comment|// at least as great as the number of rows.
return|return
operator|(
name|coveredCols
operator|==
name|cols
operator|||
name|coveredCols
operator|>=
name|rows
operator|)
return|;
block|}
comment|/**    * Corresponds to step 1 of the original algorithm.    * @return false if all zeroes are covered    */
specifier|private
name|boolean
name|stepOne
parameter_list|()
block|{
while|while
condition|(
literal|true
condition|)
block|{
name|Pair
argument_list|<
name|Integer
argument_list|,
name|Integer
argument_list|>
name|zero
init|=
name|findUncoveredZero
argument_list|()
decl_stmt|;
if|if
condition|(
name|zero
operator|==
literal|null
condition|)
block|{
comment|// No uncovered zeroes, need to manipulate the cost matrix in step
comment|// three.
return|return
literal|false
return|;
block|}
else|else
block|{
comment|// Prime the uncovered zero and find a starred zero in the same row.
name|mask
index|[
name|zero
operator|.
name|getFirst
argument_list|()
index|]
index|[
name|zero
operator|.
name|getSecond
argument_list|()
index|]
operator|=
name|PRIME
expr_stmt|;
name|Pair
argument_list|<
name|Integer
argument_list|,
name|Integer
argument_list|>
name|star
init|=
name|starInRow
argument_list|(
name|zero
operator|.
name|getFirst
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|star
operator|!=
literal|null
condition|)
block|{
comment|// Cover the row with both the newly primed zero and the starred zero.
comment|// Since this is the only place where zeroes are primed, and we cover
comment|// it here, and rows are only uncovered when primes are erased, then
comment|// there can be at most one primed uncovered zero.
name|rowsCovered
index|[
name|star
operator|.
name|getFirst
argument_list|()
index|]
operator|=
literal|true
expr_stmt|;
name|colsCovered
index|[
name|star
operator|.
name|getSecond
argument_list|()
index|]
operator|=
literal|false
expr_stmt|;
name|updateMin
argument_list|(
name|star
operator|.
name|getFirst
argument_list|()
argument_list|,
name|star
operator|.
name|getSecond
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// Will go to step two after, where a path will be constructed,
comment|// starting from the uncovered primed zero (there is only one). Since
comment|// we have already found it, save it as the first node in the path.
name|path
operator|.
name|clear
argument_list|()
expr_stmt|;
name|path
operator|.
name|offerLast
argument_list|(
operator|new
name|Pair
argument_list|<
name|Integer
argument_list|,
name|Integer
argument_list|>
argument_list|(
name|zero
operator|.
name|getFirst
argument_list|()
argument_list|,
name|zero
operator|.
name|getSecond
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
block|}
block|}
block|}
comment|/**    * Corresponds to step 2 of the original algorithm.    */
specifier|private
name|void
name|stepTwo
parameter_list|()
block|{
comment|// Construct a path of alternating starred zeroes and primed zeroes, where
comment|// each starred zero is in the same column as the previous primed zero, and
comment|// each primed zero is in the same row as the previous starred zero. The
comment|// path will always end in a primed zero.
while|while
condition|(
literal|true
condition|)
block|{
name|Pair
argument_list|<
name|Integer
argument_list|,
name|Integer
argument_list|>
name|star
init|=
name|starInCol
argument_list|(
name|path
operator|.
name|getLast
argument_list|()
operator|.
name|getSecond
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|star
operator|!=
literal|null
condition|)
block|{
name|path
operator|.
name|offerLast
argument_list|(
name|star
argument_list|)
expr_stmt|;
block|}
else|else
block|{
break|break;
block|}
name|Pair
argument_list|<
name|Integer
argument_list|,
name|Integer
argument_list|>
name|prime
init|=
name|primeInRow
argument_list|(
name|path
operator|.
name|getLast
argument_list|()
operator|.
name|getFirst
argument_list|()
argument_list|)
decl_stmt|;
name|path
operator|.
name|offerLast
argument_list|(
name|prime
argument_list|)
expr_stmt|;
block|}
comment|// Augment path - unmask all starred zeroes and star all primed zeroes. All
comment|// nodes in the path will be either starred or primed zeroes. The set of
comment|// starred zeroes is independent and now one larger than before.
for|for
control|(
name|Pair
argument_list|<
name|Integer
argument_list|,
name|Integer
argument_list|>
name|p
range|:
name|path
control|)
block|{
if|if
condition|(
name|mask
index|[
name|p
operator|.
name|getFirst
argument_list|()
index|]
index|[
name|p
operator|.
name|getSecond
argument_list|()
index|]
operator|==
name|STAR
condition|)
block|{
name|mask
index|[
name|p
operator|.
name|getFirst
argument_list|()
index|]
index|[
name|p
operator|.
name|getSecond
argument_list|()
index|]
operator|=
name|NONE
expr_stmt|;
block|}
else|else
block|{
name|mask
index|[
name|p
operator|.
name|getFirst
argument_list|()
index|]
index|[
name|p
operator|.
name|getSecond
argument_list|()
index|]
operator|=
name|STAR
expr_stmt|;
block|}
block|}
comment|// Clear all covers from rows and columns.
name|Arrays
operator|.
name|fill
argument_list|(
name|rowsCovered
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|Arrays
operator|.
name|fill
argument_list|(
name|colsCovered
argument_list|,
literal|false
argument_list|)
expr_stmt|;
comment|// Remove the prime mask from all primed zeroes.
for|for
control|(
name|int
name|r
init|=
literal|0
init|;
name|r
operator|<
name|rows
condition|;
name|r
operator|++
control|)
block|{
for|for
control|(
name|int
name|c
init|=
literal|0
init|;
name|c
operator|<
name|cols
condition|;
name|c
operator|++
control|)
block|{
if|if
condition|(
name|mask
index|[
name|r
index|]
index|[
name|c
index|]
operator|==
name|PRIME
condition|)
block|{
name|mask
index|[
name|r
index|]
index|[
name|c
index|]
operator|=
name|NONE
expr_stmt|;
block|}
block|}
block|}
block|}
comment|/**    * Corresponds to step 3 of the original algorithm.    */
specifier|private
name|void
name|stepThree
parameter_list|()
block|{
comment|// Find the minimum uncovered cost.
name|float
name|min
init|=
name|leastInRow
index|[
literal|0
index|]
decl_stmt|;
for|for
control|(
name|int
name|r
init|=
literal|1
init|;
name|r
operator|<
name|rows
condition|;
name|r
operator|++
control|)
block|{
if|if
condition|(
name|leastInRow
index|[
name|r
index|]
operator|<
name|min
condition|)
block|{
name|min
operator|=
name|leastInRow
index|[
name|r
index|]
expr_stmt|;
block|}
block|}
comment|// Add the minimum cost to each of the costs in a covered row, or subtract
comment|// the minimum cost from each of the costs in an uncovered column. As an
comment|// optimization, do not actually modify the cost matrix yet, but track the
comment|// adjustments that need to be made to each row and column.
for|for
control|(
name|int
name|r
init|=
literal|0
init|;
name|r
operator|<
name|rows
condition|;
name|r
operator|++
control|)
block|{
if|if
condition|(
name|rowsCovered
index|[
name|r
index|]
condition|)
block|{
name|rowAdjust
index|[
name|r
index|]
operator|+=
name|min
expr_stmt|;
block|}
block|}
for|for
control|(
name|int
name|c
init|=
literal|0
init|;
name|c
operator|<
name|cols
condition|;
name|c
operator|++
control|)
block|{
if|if
condition|(
operator|!
name|colsCovered
index|[
name|c
index|]
condition|)
block|{
name|colAdjust
index|[
name|c
index|]
operator|-=
name|min
expr_stmt|;
block|}
block|}
comment|// Since the cost matrix is not being updated yet, the minimum uncovered
comment|// cost per row must be updated.
for|for
control|(
name|int
name|r
init|=
literal|0
init|;
name|r
operator|<
name|rows
condition|;
name|r
operator|++
control|)
block|{
if|if
condition|(
operator|!
name|colsCovered
index|[
name|leastInRowIndex
index|[
name|r
index|]
index|]
condition|)
block|{
comment|// The least value in this row was in an uncovered column, meaning that
comment|// it would have had the minimum value subtracted from it, and therefore
comment|// will still be the minimum value in that row.
name|leastInRow
index|[
name|r
index|]
operator|-=
name|min
expr_stmt|;
block|}
else|else
block|{
comment|// The least value in this row was in a covered column and would not
comment|// have had the minimum value subtracted from it, so the minimum value
comment|// could be some in another column.
for|for
control|(
name|int
name|c
init|=
literal|0
init|;
name|c
operator|<
name|cols
condition|;
name|c
operator|++
control|)
block|{
if|if
condition|(
name|cost
index|[
name|r
index|]
index|[
name|c
index|]
operator|+
name|colAdjust
index|[
name|c
index|]
operator|+
name|rowAdjust
index|[
name|r
index|]
operator|<
name|leastInRow
index|[
name|r
index|]
condition|)
block|{
name|leastInRow
index|[
name|r
index|]
operator|=
name|cost
index|[
name|r
index|]
index|[
name|c
index|]
operator|+
name|colAdjust
index|[
name|c
index|]
operator|+
name|rowAdjust
index|[
name|r
index|]
expr_stmt|;
name|leastInRowIndex
index|[
name|r
index|]
operator|=
name|c
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
comment|/**    * Find a zero cost assignment which is not covered. If there are no zero cost    * assignments which are uncovered, then null will be returned.    * @return pair of row and column indices of an uncovered zero or null    */
specifier|private
name|Pair
argument_list|<
name|Integer
argument_list|,
name|Integer
argument_list|>
name|findUncoveredZero
parameter_list|()
block|{
for|for
control|(
name|int
name|r
init|=
literal|0
init|;
name|r
operator|<
name|rows
condition|;
name|r
operator|++
control|)
block|{
if|if
condition|(
name|leastInRow
index|[
name|r
index|]
operator|==
literal|0
condition|)
block|{
return|return
operator|new
name|Pair
argument_list|<
name|Integer
argument_list|,
name|Integer
argument_list|>
argument_list|(
name|r
argument_list|,
name|leastInRowIndex
index|[
name|r
index|]
argument_list|)
return|;
block|}
block|}
return|return
literal|null
return|;
block|}
comment|/**    * A specified row has become covered, and a specified column has become    * uncovered. The least value per row may need to be updated.    * @param row the index of the row which was just covered    * @param col the index of the column which was just uncovered    */
specifier|private
name|void
name|updateMin
parameter_list|(
name|int
name|row
parameter_list|,
name|int
name|col
parameter_list|)
block|{
comment|// If the row is covered we want to ignore it as far as least values go.
name|leastInRow
index|[
name|row
index|]
operator|=
name|Float
operator|.
name|POSITIVE_INFINITY
expr_stmt|;
for|for
control|(
name|int
name|r
init|=
literal|0
init|;
name|r
operator|<
name|rows
condition|;
name|r
operator|++
control|)
block|{
comment|// Since the column has only just been uncovered, it could not have any
comment|// pending adjustments. Only covered rows can have pending adjustments
comment|// and covered costs do not count toward row minimums. Therefore, we do
comment|// not need to consider rowAdjust[r] or colAdjust[col].
if|if
condition|(
operator|!
name|rowsCovered
index|[
name|r
index|]
operator|&&
name|cost
index|[
name|r
index|]
index|[
name|col
index|]
operator|<
name|leastInRow
index|[
name|r
index|]
condition|)
block|{
name|leastInRow
index|[
name|r
index|]
operator|=
name|cost
index|[
name|r
index|]
index|[
name|col
index|]
expr_stmt|;
name|leastInRowIndex
index|[
name|r
index|]
operator|=
name|col
expr_stmt|;
block|}
block|}
block|}
comment|/**    * Find a starred zero in a specified row. If there are no starred zeroes in    * the specified row, then null will be returned.    * @param r the index of the row to be searched    * @return pair of row and column indices of starred zero or null    */
specifier|private
name|Pair
argument_list|<
name|Integer
argument_list|,
name|Integer
argument_list|>
name|starInRow
parameter_list|(
name|int
name|r
parameter_list|)
block|{
for|for
control|(
name|int
name|c
init|=
literal|0
init|;
name|c
operator|<
name|cols
condition|;
name|c
operator|++
control|)
block|{
if|if
condition|(
name|mask
index|[
name|r
index|]
index|[
name|c
index|]
operator|==
name|STAR
condition|)
block|{
return|return
operator|new
name|Pair
argument_list|<
name|Integer
argument_list|,
name|Integer
argument_list|>
argument_list|(
name|r
argument_list|,
name|c
argument_list|)
return|;
block|}
block|}
return|return
literal|null
return|;
block|}
comment|/**    * Find a starred zero in the specified column. If there are no starred zeroes    * in the specified row, then null will be returned.    * @param c the index of the column to be searched    * @return pair of row and column indices of starred zero or null    */
specifier|private
name|Pair
argument_list|<
name|Integer
argument_list|,
name|Integer
argument_list|>
name|starInCol
parameter_list|(
name|int
name|c
parameter_list|)
block|{
for|for
control|(
name|int
name|r
init|=
literal|0
init|;
name|r
operator|<
name|rows
condition|;
name|r
operator|++
control|)
block|{
if|if
condition|(
name|mask
index|[
name|r
index|]
index|[
name|c
index|]
operator|==
name|STAR
condition|)
block|{
return|return
operator|new
name|Pair
argument_list|<
name|Integer
argument_list|,
name|Integer
argument_list|>
argument_list|(
name|r
argument_list|,
name|c
argument_list|)
return|;
block|}
block|}
return|return
literal|null
return|;
block|}
comment|/**    * Find a primed zero in the specified row. If there are no primed zeroes in    * the specified row, then null will be returned.    * @param r the index of the row to be searched    * @return pair of row and column indices of primed zero or null    */
specifier|private
name|Pair
argument_list|<
name|Integer
argument_list|,
name|Integer
argument_list|>
name|primeInRow
parameter_list|(
name|int
name|r
parameter_list|)
block|{
for|for
control|(
name|int
name|c
init|=
literal|0
init|;
name|c
operator|<
name|cols
condition|;
name|c
operator|++
control|)
block|{
if|if
condition|(
name|mask
index|[
name|r
index|]
index|[
name|c
index|]
operator|==
name|PRIME
condition|)
block|{
return|return
operator|new
name|Pair
argument_list|<
name|Integer
argument_list|,
name|Integer
argument_list|>
argument_list|(
name|r
argument_list|,
name|c
argument_list|)
return|;
block|}
block|}
return|return
literal|null
return|;
block|}
block|}
end_class

end_unit

