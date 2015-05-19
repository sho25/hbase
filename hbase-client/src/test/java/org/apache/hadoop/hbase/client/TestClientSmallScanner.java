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
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|conf
operator|.
name|Configuration
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
name|*
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
name|KeyValue
operator|.
name|Type
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
name|client
operator|.
name|ClientSmallScanner
operator|.
name|SmallScannerCallableFactory
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
name|client
operator|.
name|metrics
operator|.
name|ScanMetrics
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
name|ipc
operator|.
name|RpcControllerFactory
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
name|SmallTests
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|After
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Before
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
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
name|mockito
operator|.
name|Mockito
import|;
end_import

begin_import
import|import
name|org
operator|.
name|mockito
operator|.
name|invocation
operator|.
name|InvocationOnMock
import|;
end_import

begin_import
import|import
name|org
operator|.
name|mockito
operator|.
name|stubbing
operator|.
name|Answer
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
name|nio
operator|.
name|charset
operator|.
name|StandardCharsets
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
name|concurrent
operator|.
name|ExecutorService
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|Executors
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertEquals
import|;
end_import

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

begin_comment
comment|/**  * Test the ClientSmallScanner.  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
name|SmallTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestClientSmallScanner
block|{
name|Scan
name|scan
decl_stmt|;
name|ExecutorService
name|pool
decl_stmt|;
name|Configuration
name|conf
decl_stmt|;
name|ClusterConnection
name|clusterConn
decl_stmt|;
name|RpcRetryingCallerFactory
name|rpcFactory
decl_stmt|;
name|RpcControllerFactory
name|controllerFactory
decl_stmt|;
name|RpcRetryingCaller
argument_list|<
name|Result
index|[]
argument_list|>
name|caller
decl_stmt|;
annotation|@
name|Before
annotation|@
name|SuppressWarnings
argument_list|(
block|{
literal|"deprecation"
block|,
literal|"unchecked"
block|}
argument_list|)
specifier|public
name|void
name|setup
parameter_list|()
throws|throws
name|IOException
block|{
name|clusterConn
operator|=
name|Mockito
operator|.
name|mock
argument_list|(
name|ClusterConnection
operator|.
name|class
argument_list|)
expr_stmt|;
name|rpcFactory
operator|=
name|Mockito
operator|.
name|mock
argument_list|(
name|RpcRetryingCallerFactory
operator|.
name|class
argument_list|)
expr_stmt|;
name|controllerFactory
operator|=
name|Mockito
operator|.
name|mock
argument_list|(
name|RpcControllerFactory
operator|.
name|class
argument_list|)
expr_stmt|;
name|pool
operator|=
name|Executors
operator|.
name|newSingleThreadExecutor
argument_list|()
expr_stmt|;
name|scan
operator|=
operator|new
name|Scan
argument_list|()
expr_stmt|;
name|conf
operator|=
operator|new
name|Configuration
argument_list|()
expr_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|clusterConn
operator|.
name|getConfiguration
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|conf
argument_list|)
expr_stmt|;
comment|// Mock out the RpcCaller
name|caller
operator|=
name|Mockito
operator|.
name|mock
argument_list|(
name|RpcRetryingCaller
operator|.
name|class
argument_list|)
expr_stmt|;
comment|// Return the mock from the factory
name|Mockito
operator|.
name|when
argument_list|(
name|rpcFactory
operator|.
expr|<
name|Result
index|[]
operator|>
name|newCaller
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|caller
argument_list|)
expr_stmt|;
block|}
annotation|@
name|After
specifier|public
name|void
name|teardown
parameter_list|()
block|{
if|if
condition|(
literal|null
operator|!=
name|pool
condition|)
block|{
name|pool
operator|.
name|shutdownNow
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * Create a simple Answer which returns true the first time, and false every time after.    */
specifier|private
name|Answer
argument_list|<
name|Boolean
argument_list|>
name|createTrueThenFalseAnswer
parameter_list|()
block|{
return|return
operator|new
name|Answer
argument_list|<
name|Boolean
argument_list|>
argument_list|()
block|{
name|boolean
name|first
init|=
literal|true
decl_stmt|;
annotation|@
name|Override
specifier|public
name|Boolean
name|answer
parameter_list|(
name|InvocationOnMock
name|invocation
parameter_list|)
block|{
if|if
condition|(
name|first
condition|)
block|{
name|first
operator|=
literal|false
expr_stmt|;
return|return
literal|true
return|;
block|}
return|return
literal|false
return|;
block|}
block|}
return|;
block|}
specifier|private
name|SmallScannerCallableFactory
name|getFactory
parameter_list|(
specifier|final
name|ScannerCallableWithReplicas
name|callableWithReplicas
parameter_list|)
block|{
return|return
operator|new
name|SmallScannerCallableFactory
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|ScannerCallableWithReplicas
name|getCallable
parameter_list|(
name|ClusterConnection
name|connection
parameter_list|,
name|TableName
name|table
parameter_list|,
name|Scan
name|scan
parameter_list|,
name|ScanMetrics
name|scanMetrics
parameter_list|,
name|byte
index|[]
name|localStartKey
parameter_list|,
name|int
name|cacheNum
parameter_list|,
name|RpcControllerFactory
name|controllerFactory
parameter_list|,
name|ExecutorService
name|pool
parameter_list|,
name|int
name|primaryOperationTimeout
parameter_list|,
name|int
name|retries
parameter_list|,
name|int
name|scannerTimeout
parameter_list|,
name|Configuration
name|conf
parameter_list|,
name|RpcRetryingCaller
argument_list|<
name|Result
index|[]
argument_list|>
name|caller
parameter_list|)
block|{
return|return
name|callableWithReplicas
return|;
block|}
block|}
return|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testContextPresent
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|KeyValue
name|kv1
init|=
operator|new
name|KeyValue
argument_list|(
literal|"row1"
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|"cf"
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|"cq"
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|1
argument_list|,
name|Type
operator|.
name|Maximum
argument_list|)
decl_stmt|,
name|kv2
init|=
operator|new
name|KeyValue
argument_list|(
literal|"row2"
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|"cf"
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|"cq"
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|1
argument_list|,
name|Type
operator|.
name|Maximum
argument_list|)
decl_stmt|,
name|kv3
init|=
operator|new
name|KeyValue
argument_list|(
literal|"row3"
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|"cf"
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|"cq"
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|1
argument_list|,
name|Type
operator|.
name|Maximum
argument_list|)
decl_stmt|;
name|ScannerCallableWithReplicas
name|callableWithReplicas
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|ScannerCallableWithReplicas
operator|.
name|class
argument_list|)
decl_stmt|;
comment|// Mock out the RpcCaller
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
name|RpcRetryingCaller
argument_list|<
name|Result
index|[]
argument_list|>
name|caller
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|RpcRetryingCaller
operator|.
name|class
argument_list|)
decl_stmt|;
comment|// Return the mock from the factory
name|Mockito
operator|.
name|when
argument_list|(
name|rpcFactory
operator|.
expr|<
name|Result
index|[]
operator|>
name|newCaller
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|caller
argument_list|)
expr_stmt|;
name|SmallScannerCallableFactory
name|factory
init|=
name|getFactory
argument_list|(
name|callableWithReplicas
argument_list|)
decl_stmt|;
comment|// Intentionally leave a "default" caching size in the Scan. No matter the value, we
comment|// should continue based on the server context
try|try
init|(
name|ClientSmallScanner
name|css
init|=
operator|new
name|ClientSmallScanner
argument_list|(
name|conf
argument_list|,
name|scan
argument_list|,
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"table"
argument_list|)
argument_list|,
name|clusterConn
argument_list|,
name|rpcFactory
argument_list|,
name|controllerFactory
argument_list|,
name|pool
argument_list|,
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
init|)
block|{
name|css
operator|.
name|setScannerCallableFactory
argument_list|(
name|factory
argument_list|)
expr_stmt|;
comment|// Return some data the first time, less the second, and none after that
name|Mockito
operator|.
name|when
argument_list|(
name|caller
operator|.
name|callWithoutRetries
argument_list|(
name|callableWithReplicas
argument_list|,
name|css
operator|.
name|getScannerTimeout
argument_list|()
argument_list|)
argument_list|)
operator|.
name|thenAnswer
argument_list|(
operator|new
name|Answer
argument_list|<
name|Result
index|[]
argument_list|>
argument_list|()
block|{
name|int
name|count
init|=
literal|0
decl_stmt|;
annotation|@
name|Override
specifier|public
name|Result
index|[]
name|answer
parameter_list|(
name|InvocationOnMock
name|invocation
parameter_list|)
block|{
name|Result
index|[]
name|results
decl_stmt|;
if|if
condition|(
literal|0
operator|==
name|count
condition|)
block|{
name|results
operator|=
operator|new
name|Result
index|[]
block|{
name|Result
operator|.
name|create
argument_list|(
operator|new
name|Cell
index|[]
block|{
name|kv1
block|}
argument_list|)
block|,
name|Result
operator|.
name|create
argument_list|(
operator|new
name|Cell
index|[]
block|{
name|kv2
block|}
argument_list|)
block|}
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|1
operator|==
name|count
condition|)
block|{
name|results
operator|=
operator|new
name|Result
index|[]
block|{
name|Result
operator|.
name|create
argument_list|(
operator|new
name|Cell
index|[]
block|{
name|kv3
block|}
argument_list|)
block|}
expr_stmt|;
block|}
else|else
block|{
name|results
operator|=
operator|new
name|Result
index|[
literal|0
index|]
expr_stmt|;
block|}
name|count
operator|++
expr_stmt|;
return|return
name|results
return|;
block|}
block|}
argument_list|)
expr_stmt|;
comment|// Pass back the context always
name|Mockito
operator|.
name|when
argument_list|(
name|callableWithReplicas
operator|.
name|hasMoreResultsContext
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
literal|true
argument_list|)
expr_stmt|;
comment|// Only have more results the first time
name|Mockito
operator|.
name|when
argument_list|(
name|callableWithReplicas
operator|.
name|getServerHasMoreResults
argument_list|()
argument_list|)
operator|.
name|thenAnswer
argument_list|(
name|createTrueThenFalseAnswer
argument_list|()
argument_list|)
expr_stmt|;
comment|// A mocked HRegionInfo so ClientSmallScanner#nextScanner(...) works right
name|HRegionInfo
name|regionInfo
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|HRegionInfo
operator|.
name|class
argument_list|)
decl_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|callableWithReplicas
operator|.
name|getHRegionInfo
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|regionInfo
argument_list|)
expr_stmt|;
comment|// Trigger the "no more data" branch for #nextScanner(...)
name|Mockito
operator|.
name|when
argument_list|(
name|regionInfo
operator|.
name|getEndKey
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|HConstants
operator|.
name|EMPTY_BYTE_ARRAY
argument_list|)
expr_stmt|;
name|css
operator|.
name|loadCache
argument_list|()
expr_stmt|;
name|Queue
argument_list|<
name|Result
argument_list|>
name|results
init|=
name|css
operator|.
name|cache
decl_stmt|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|results
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<=
literal|3
condition|;
name|i
operator|++
control|)
block|{
name|Result
name|result
init|=
name|results
operator|.
name|poll
argument_list|()
decl_stmt|;
name|byte
index|[]
name|row
init|=
name|result
operator|.
name|getRow
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|"row"
operator|+
name|i
argument_list|,
operator|new
name|String
argument_list|(
name|row
argument_list|,
name|StandardCharsets
operator|.
name|UTF_8
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|result
operator|.
name|getMap
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|assertTrue
argument_list|(
name|css
operator|.
name|closed
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testNoContextFewerRecords
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|KeyValue
name|kv1
init|=
operator|new
name|KeyValue
argument_list|(
literal|"row1"
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|"cf"
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|"cq"
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|1
argument_list|,
name|Type
operator|.
name|Maximum
argument_list|)
decl_stmt|,
name|kv2
init|=
operator|new
name|KeyValue
argument_list|(
literal|"row2"
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|"cf"
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|"cq"
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|1
argument_list|,
name|Type
operator|.
name|Maximum
argument_list|)
decl_stmt|,
name|kv3
init|=
operator|new
name|KeyValue
argument_list|(
literal|"row3"
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|"cf"
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|"cq"
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|1
argument_list|,
name|Type
operator|.
name|Maximum
argument_list|)
decl_stmt|;
name|ScannerCallableWithReplicas
name|callableWithReplicas
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|ScannerCallableWithReplicas
operator|.
name|class
argument_list|)
decl_stmt|;
comment|// While the server returns 2 records per batch, we expect more records.
name|scan
operator|.
name|setCaching
argument_list|(
literal|2
argument_list|)
expr_stmt|;
name|SmallScannerCallableFactory
name|factory
init|=
name|getFactory
argument_list|(
name|callableWithReplicas
argument_list|)
decl_stmt|;
try|try
init|(
name|ClientSmallScanner
name|css
init|=
operator|new
name|ClientSmallScanner
argument_list|(
name|conf
argument_list|,
name|scan
argument_list|,
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"table"
argument_list|)
argument_list|,
name|clusterConn
argument_list|,
name|rpcFactory
argument_list|,
name|controllerFactory
argument_list|,
name|pool
argument_list|,
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
init|)
block|{
name|css
operator|.
name|setScannerCallableFactory
argument_list|(
name|factory
argument_list|)
expr_stmt|;
comment|// Return some data the first time, less the second, and none after that
name|Mockito
operator|.
name|when
argument_list|(
name|caller
operator|.
name|callWithoutRetries
argument_list|(
name|callableWithReplicas
argument_list|,
name|css
operator|.
name|getScannerTimeout
argument_list|()
argument_list|)
argument_list|)
operator|.
name|thenAnswer
argument_list|(
operator|new
name|Answer
argument_list|<
name|Result
index|[]
argument_list|>
argument_list|()
block|{
name|int
name|count
init|=
literal|0
decl_stmt|;
annotation|@
name|Override
specifier|public
name|Result
index|[]
name|answer
parameter_list|(
name|InvocationOnMock
name|invocation
parameter_list|)
block|{
name|Result
index|[]
name|results
decl_stmt|;
if|if
condition|(
literal|0
operator|==
name|count
condition|)
block|{
name|results
operator|=
operator|new
name|Result
index|[]
block|{
name|Result
operator|.
name|create
argument_list|(
operator|new
name|Cell
index|[]
block|{
name|kv1
block|}
argument_list|)
block|,
name|Result
operator|.
name|create
argument_list|(
operator|new
name|Cell
index|[]
block|{
name|kv2
block|}
argument_list|)
block|}
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|1
operator|==
name|count
condition|)
block|{
comment|// Return fewer records than expected (2)
name|results
operator|=
operator|new
name|Result
index|[]
block|{
name|Result
operator|.
name|create
argument_list|(
operator|new
name|Cell
index|[]
block|{
name|kv3
block|}
argument_list|)
block|}
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Should not fetch a third batch from the server"
argument_list|)
throw|;
block|}
name|count
operator|++
expr_stmt|;
return|return
name|results
return|;
block|}
block|}
argument_list|)
expr_stmt|;
comment|// Server doesn't return the context
name|Mockito
operator|.
name|when
argument_list|(
name|callableWithReplicas
operator|.
name|hasMoreResultsContext
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
literal|false
argument_list|)
expr_stmt|;
comment|// Only have more results the first time
name|Mockito
operator|.
name|when
argument_list|(
name|callableWithReplicas
operator|.
name|getServerHasMoreResults
argument_list|()
argument_list|)
operator|.
name|thenThrow
argument_list|(
operator|new
name|RuntimeException
argument_list|(
literal|"Should not be called"
argument_list|)
argument_list|)
expr_stmt|;
comment|// A mocked HRegionInfo so ClientSmallScanner#nextScanner(...) works right
name|HRegionInfo
name|regionInfo
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|HRegionInfo
operator|.
name|class
argument_list|)
decl_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|callableWithReplicas
operator|.
name|getHRegionInfo
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|regionInfo
argument_list|)
expr_stmt|;
comment|// Trigger the "no more data" branch for #nextScanner(...)
name|Mockito
operator|.
name|when
argument_list|(
name|regionInfo
operator|.
name|getEndKey
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|HConstants
operator|.
name|EMPTY_BYTE_ARRAY
argument_list|)
expr_stmt|;
name|css
operator|.
name|loadCache
argument_list|()
expr_stmt|;
name|Queue
argument_list|<
name|Result
argument_list|>
name|results
init|=
name|css
operator|.
name|cache
decl_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|results
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<=
literal|2
condition|;
name|i
operator|++
control|)
block|{
name|Result
name|result
init|=
name|results
operator|.
name|poll
argument_list|()
decl_stmt|;
name|byte
index|[]
name|row
init|=
name|result
operator|.
name|getRow
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|"row"
operator|+
name|i
argument_list|,
operator|new
name|String
argument_list|(
name|row
argument_list|,
name|StandardCharsets
operator|.
name|UTF_8
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|result
operator|.
name|getMap
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// "consume" the results we verified
name|results
operator|.
name|clear
argument_list|()
expr_stmt|;
name|css
operator|.
name|loadCache
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|results
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Result
name|result
init|=
name|results
operator|.
name|peek
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|"row3"
argument_list|,
operator|new
name|String
argument_list|(
name|result
operator|.
name|getRow
argument_list|()
argument_list|,
name|StandardCharsets
operator|.
name|UTF_8
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|result
operator|.
name|getMap
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|css
operator|.
name|closed
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testNoContextNoRecords
parameter_list|()
throws|throws
name|Exception
block|{
name|ScannerCallableWithReplicas
name|callableWithReplicas
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|ScannerCallableWithReplicas
operator|.
name|class
argument_list|)
decl_stmt|;
comment|// While the server return 2 records per RPC, we expect there to be more records.
name|scan
operator|.
name|setCaching
argument_list|(
literal|2
argument_list|)
expr_stmt|;
name|SmallScannerCallableFactory
name|factory
init|=
name|getFactory
argument_list|(
name|callableWithReplicas
argument_list|)
decl_stmt|;
try|try
init|(
name|ClientSmallScanner
name|css
init|=
operator|new
name|ClientSmallScanner
argument_list|(
name|conf
argument_list|,
name|scan
argument_list|,
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"table"
argument_list|)
argument_list|,
name|clusterConn
argument_list|,
name|rpcFactory
argument_list|,
name|controllerFactory
argument_list|,
name|pool
argument_list|,
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
init|)
block|{
name|css
operator|.
name|setScannerCallableFactory
argument_list|(
name|factory
argument_list|)
expr_stmt|;
comment|// Return some data the first time, less the second, and none after that
name|Mockito
operator|.
name|when
argument_list|(
name|caller
operator|.
name|callWithoutRetries
argument_list|(
name|callableWithReplicas
argument_list|,
name|css
operator|.
name|getScannerTimeout
argument_list|()
argument_list|)
argument_list|)
operator|.
name|thenReturn
argument_list|(
operator|new
name|Result
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
comment|// Server doesn't return the context
name|Mockito
operator|.
name|when
argument_list|(
name|callableWithReplicas
operator|.
name|hasMoreResultsContext
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
literal|false
argument_list|)
expr_stmt|;
comment|// Only have more results the first time
name|Mockito
operator|.
name|when
argument_list|(
name|callableWithReplicas
operator|.
name|getServerHasMoreResults
argument_list|()
argument_list|)
operator|.
name|thenThrow
argument_list|(
operator|new
name|RuntimeException
argument_list|(
literal|"Should not be called"
argument_list|)
argument_list|)
expr_stmt|;
comment|// A mocked HRegionInfo so ClientSmallScanner#nextScanner(...) works right
name|HRegionInfo
name|regionInfo
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|HRegionInfo
operator|.
name|class
argument_list|)
decl_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|callableWithReplicas
operator|.
name|getHRegionInfo
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|regionInfo
argument_list|)
expr_stmt|;
comment|// Trigger the "no more data" branch for #nextScanner(...)
name|Mockito
operator|.
name|when
argument_list|(
name|regionInfo
operator|.
name|getEndKey
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|HConstants
operator|.
name|EMPTY_BYTE_ARRAY
argument_list|)
expr_stmt|;
name|css
operator|.
name|loadCache
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|css
operator|.
name|cache
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|css
operator|.
name|closed
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testContextNoRecords
parameter_list|()
throws|throws
name|Exception
block|{
name|ScannerCallableWithReplicas
name|callableWithReplicas
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|ScannerCallableWithReplicas
operator|.
name|class
argument_list|)
decl_stmt|;
name|SmallScannerCallableFactory
name|factory
init|=
name|getFactory
argument_list|(
name|callableWithReplicas
argument_list|)
decl_stmt|;
try|try
init|(
name|ClientSmallScanner
name|css
init|=
operator|new
name|ClientSmallScanner
argument_list|(
name|conf
argument_list|,
name|scan
argument_list|,
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"table"
argument_list|)
argument_list|,
name|clusterConn
argument_list|,
name|rpcFactory
argument_list|,
name|controllerFactory
argument_list|,
name|pool
argument_list|,
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
init|)
block|{
name|css
operator|.
name|setScannerCallableFactory
argument_list|(
name|factory
argument_list|)
expr_stmt|;
comment|// Return some data the first time, less the second, and none after that
name|Mockito
operator|.
name|when
argument_list|(
name|caller
operator|.
name|callWithoutRetries
argument_list|(
name|callableWithReplicas
argument_list|,
name|css
operator|.
name|getScannerTimeout
argument_list|()
argument_list|)
argument_list|)
operator|.
name|thenReturn
argument_list|(
operator|new
name|Result
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
comment|// Server doesn't return the context
name|Mockito
operator|.
name|when
argument_list|(
name|callableWithReplicas
operator|.
name|hasMoreResultsContext
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
literal|true
argument_list|)
expr_stmt|;
comment|// Only have more results the first time
name|Mockito
operator|.
name|when
argument_list|(
name|callableWithReplicas
operator|.
name|getServerHasMoreResults
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
literal|false
argument_list|)
expr_stmt|;
comment|// A mocked HRegionInfo so ClientSmallScanner#nextScanner(...) works right
name|HRegionInfo
name|regionInfo
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|HRegionInfo
operator|.
name|class
argument_list|)
decl_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|callableWithReplicas
operator|.
name|getHRegionInfo
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|regionInfo
argument_list|)
expr_stmt|;
comment|// Trigger the "no more data" branch for #nextScanner(...)
name|Mockito
operator|.
name|when
argument_list|(
name|regionInfo
operator|.
name|getEndKey
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|HConstants
operator|.
name|EMPTY_BYTE_ARRAY
argument_list|)
expr_stmt|;
name|css
operator|.
name|loadCache
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|css
operator|.
name|cache
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|css
operator|.
name|closed
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

