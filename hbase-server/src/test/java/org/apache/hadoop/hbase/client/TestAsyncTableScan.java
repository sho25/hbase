begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|client
package|;
end_package

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertTrue
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Throwables
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|UncheckedIOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayDeque
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
import|;
end_import

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
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Queue
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|function
operator|.
name|Supplier
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|stream
operator|.
name|Collectors
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|stream
operator|.
name|IntStream
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
name|testclassification
operator|.
name|ClientTests
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
name|testclassification
operator|.
name|MediumTests
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|experimental
operator|.
name|categories
operator|.
name|Category
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|runner
operator|.
name|RunWith
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|runners
operator|.
name|Parameterized
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|runners
operator|.
name|Parameterized
operator|.
name|Parameter
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|runners
operator|.
name|Parameterized
operator|.
name|Parameters
import|;
end_import

begin_class
annotation|@
name|RunWith
argument_list|(
name|Parameterized
operator|.
name|class
argument_list|)
annotation|@
name|Category
argument_list|(
block|{
name|MediumTests
operator|.
name|class
block|,
name|ClientTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestAsyncTableScan
extends|extends
name|AbstractTestAsyncTableScan
block|{
specifier|private
specifier|static
specifier|final
class|class
name|SimpleScanResultConsumer
implements|implements
name|ScanResultConsumer
block|{
specifier|private
specifier|final
name|Queue
argument_list|<
name|Result
argument_list|>
name|queue
init|=
operator|new
name|ArrayDeque
argument_list|<>
argument_list|()
decl_stmt|;
specifier|private
name|boolean
name|finished
decl_stmt|;
specifier|private
name|Throwable
name|error
decl_stmt|;
annotation|@
name|Override
specifier|public
specifier|synchronized
name|boolean
name|onNext
parameter_list|(
name|Result
index|[]
name|results
parameter_list|)
block|{
for|for
control|(
name|Result
name|result
range|:
name|results
control|)
block|{
name|queue
operator|.
name|offer
argument_list|(
name|result
argument_list|)
expr_stmt|;
block|}
name|notifyAll
argument_list|()
expr_stmt|;
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|onHeartbeat
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|public
specifier|synchronized
name|void
name|onError
parameter_list|(
name|Throwable
name|error
parameter_list|)
block|{
name|finished
operator|=
literal|true
expr_stmt|;
name|this
operator|.
name|error
operator|=
name|error
expr_stmt|;
name|notifyAll
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
specifier|synchronized
name|void
name|onComplete
parameter_list|()
block|{
name|finished
operator|=
literal|true
expr_stmt|;
name|notifyAll
argument_list|()
expr_stmt|;
block|}
specifier|public
specifier|synchronized
name|Result
name|take
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
for|for
control|(
init|;
condition|;
control|)
block|{
if|if
condition|(
operator|!
name|queue
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return
name|queue
operator|.
name|poll
argument_list|()
return|;
block|}
if|if
condition|(
name|finished
condition|)
block|{
if|if
condition|(
name|error
operator|!=
literal|null
condition|)
block|{
name|Throwables
operator|.
name|propagateIfPossible
argument_list|(
name|error
argument_list|,
name|IOException
operator|.
name|class
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|IOException
argument_list|(
name|error
argument_list|)
throw|;
block|}
else|else
block|{
return|return
literal|null
return|;
block|}
block|}
name|wait
argument_list|()
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Parameter
specifier|public
name|Supplier
argument_list|<
name|Scan
argument_list|>
name|scanCreater
decl_stmt|;
annotation|@
name|Parameters
specifier|public
specifier|static
name|List
argument_list|<
name|Object
index|[]
argument_list|>
name|params
parameter_list|()
block|{
return|return
name|Arrays
operator|.
name|asList
argument_list|(
operator|new
name|Supplier
argument_list|<
name|?
argument_list|>
index|[]
block|{
name|TestAsyncTableScan
operator|::
name|createNormalScan
block|}
operator|,
operator|new
name|Supplier
argument_list|<
name|?
argument_list|>
index|[]
block|{
name|TestAsyncTableScan
operator|::
name|createBatchScan
block|}
block|)
function|;
block|}
end_class

begin_function
specifier|private
specifier|static
name|Scan
name|createNormalScan
parameter_list|()
block|{
return|return
operator|new
name|Scan
argument_list|()
return|;
block|}
end_function

begin_function
specifier|private
specifier|static
name|Scan
name|createBatchScan
parameter_list|()
block|{
return|return
operator|new
name|Scan
argument_list|()
operator|.
name|setBatch
argument_list|(
literal|1
argument_list|)
return|;
block|}
end_function

begin_function
annotation|@
name|Override
specifier|protected
name|Scan
name|createScan
parameter_list|()
block|{
return|return
name|scanCreater
operator|.
name|get
argument_list|()
return|;
block|}
end_function

begin_function
specifier|private
name|Result
name|convertToPartial
parameter_list|(
name|Result
name|result
parameter_list|)
block|{
return|return
name|Result
operator|.
name|create
argument_list|(
name|result
operator|.
name|rawCells
argument_list|()
argument_list|,
name|result
operator|.
name|getExists
argument_list|()
argument_list|,
name|result
operator|.
name|isStale
argument_list|()
argument_list|,
literal|true
argument_list|)
return|;
block|}
end_function

begin_function
annotation|@
name|Override
specifier|protected
name|List
argument_list|<
name|Result
argument_list|>
name|doScan
parameter_list|(
name|AsyncTable
name|table
parameter_list|,
name|Scan
name|scan
parameter_list|)
throws|throws
name|Exception
block|{
name|SimpleScanResultConsumer
name|scanObserver
init|=
operator|new
name|SimpleScanResultConsumer
argument_list|()
decl_stmt|;
name|table
operator|.
name|scan
argument_list|(
name|scan
argument_list|,
name|scanObserver
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|Result
argument_list|>
name|results
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|Result
name|result
init|;
operator|(
name|result
operator|=
name|scanObserver
operator|.
name|take
argument_list|()
operator|)
operator|!=
literal|null
condition|;
control|)
block|{
name|results
operator|.
name|add
argument_list|(
name|result
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|scan
operator|.
name|getBatch
argument_list|()
operator|>
literal|0
condition|)
block|{
name|assertTrue
argument_list|(
name|results
operator|.
name|size
argument_list|()
operator|%
literal|2
operator|==
literal|0
argument_list|)
expr_stmt|;
return|return
name|IntStream
operator|.
name|range
argument_list|(
literal|0
argument_list|,
name|results
operator|.
name|size
argument_list|()
operator|/
literal|2
argument_list|)
operator|.
name|mapToObj
argument_list|(
name|i
lambda|->
block|{
try|try
block|{
return|return
name|Result
operator|.
name|createCompleteResult
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|convertToPartial
argument_list|(
name|results
operator|.
name|get
argument_list|(
literal|2
operator|*
name|i
argument_list|)
argument_list|)
argument_list|,
name|convertToPartial
argument_list|(
name|results
operator|.
name|get
argument_list|(
literal|2
operator|*
name|i
operator|+
literal|1
argument_list|)
argument_list|)
argument_list|)
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|UncheckedIOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toList
argument_list|()
argument_list|)
return|;
block|}
return|return
name|results
return|;
block|}
end_function

unit|}
end_unit

