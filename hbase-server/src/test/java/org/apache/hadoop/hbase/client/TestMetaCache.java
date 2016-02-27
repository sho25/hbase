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
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|RpcController
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|ServiceException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
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
name|exceptions
operator|.
name|ClientExceptionsUtil
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
name|exceptions
operator|.
name|RegionOpeningException
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
name|protobuf
operator|.
name|generated
operator|.
name|ClientProtos
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
name|protobuf
operator|.
name|generated
operator|.
name|ClientProtos
operator|.
name|GetResponse
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
name|quotas
operator|.
name|ThrottlingException
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
name|regionserver
operator|.
name|HRegionServer
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
name|regionserver
operator|.
name|RSRpcServices
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
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|util
operator|.
name|Bytes
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
name|AfterClass
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
name|BeforeClass
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
name|List
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
name|assertNotNull
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
name|assertNull
import|;
end_import

begin_class
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
name|TestMetaCache
block|{
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|TestMetaCache
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|HBaseTestingUtility
name|TEST_UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|TableName
name|TABLE_NAME
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"test_table"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|FAMILY
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"fam1"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|QUALIFIER
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"qual"
argument_list|)
decl_stmt|;
specifier|private
name|ConnectionImplementation
name|conn
decl_stmt|;
comment|/**    * @throws java.lang.Exception    */
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|setUpBeforeClass
parameter_list|()
throws|throws
name|Exception
block|{
name|Configuration
name|conf
init|=
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|conf
operator|.
name|set
argument_list|(
literal|"hbase.client.retries.number"
argument_list|,
literal|"1"
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setStrings
argument_list|(
name|HConstants
operator|.
name|REGION_SERVER_IMPL
argument_list|,
name|RegionServerWithFakeRpcServices
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|(
literal|1
argument_list|)
expr_stmt|;
block|}
comment|/**    * @throws java.lang.Exception    */
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|tearDownAfterClass
parameter_list|()
throws|throws
name|Exception
block|{
name|TEST_UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
comment|/**    * @throws java.lang.Exception    */
annotation|@
name|Before
specifier|public
name|void
name|setup
parameter_list|()
throws|throws
name|Exception
block|{
name|conn
operator|=
operator|(
name|ConnectionImplementation
operator|)
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
name|HTableDescriptor
name|table
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|TABLE_NAME
argument_list|)
decl_stmt|;
name|HColumnDescriptor
name|fam
init|=
operator|new
name|HColumnDescriptor
argument_list|(
name|FAMILY
argument_list|)
decl_stmt|;
name|fam
operator|.
name|setMaxVersions
argument_list|(
literal|2
argument_list|)
expr_stmt|;
name|table
operator|.
name|addFamily
argument_list|(
name|fam
argument_list|)
expr_stmt|;
try|try
init|(
name|Admin
name|admin
init|=
name|conn
operator|.
name|getAdmin
argument_list|()
init|)
block|{
name|admin
operator|.
name|createTable
argument_list|(
name|table
argument_list|,
name|HBaseTestingUtility
operator|.
name|KEYS_FOR_HBA_CREATE_TABLE
argument_list|)
expr_stmt|;
block|}
name|TEST_UTIL
operator|.
name|waitUntilAllRegionsAssigned
argument_list|(
name|TABLE_NAME
argument_list|)
expr_stmt|;
block|}
comment|/**    * @throws java.lang.Exception    */
annotation|@
name|After
specifier|public
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Nothing to do.
block|}
annotation|@
name|Test
specifier|public
name|void
name|testPreserveMetaCacheOnException
parameter_list|()
throws|throws
name|Exception
block|{
name|Table
name|table
init|=
name|conn
operator|.
name|getTable
argument_list|(
name|TABLE_NAME
argument_list|)
decl_stmt|;
name|byte
index|[]
name|row
init|=
name|HBaseTestingUtility
operator|.
name|KEYS
index|[
literal|2
index|]
decl_stmt|;
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|row
argument_list|)
decl_stmt|;
name|put
operator|.
name|addColumn
argument_list|(
name|FAMILY
argument_list|,
name|QUALIFIER
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|10
argument_list|)
argument_list|)
expr_stmt|;
name|Get
name|get
init|=
operator|new
name|Get
argument_list|(
name|row
argument_list|)
decl_stmt|;
name|Append
name|append
init|=
operator|new
name|Append
argument_list|(
name|row
argument_list|)
decl_stmt|;
name|append
operator|.
name|add
argument_list|(
name|FAMILY
argument_list|,
name|QUALIFIER
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|11
argument_list|)
argument_list|)
expr_stmt|;
name|Increment
name|increment
init|=
operator|new
name|Increment
argument_list|(
name|row
argument_list|)
decl_stmt|;
name|increment
operator|.
name|addColumn
argument_list|(
name|FAMILY
argument_list|,
name|QUALIFIER
argument_list|,
literal|10
argument_list|)
expr_stmt|;
name|Delete
name|delete
init|=
operator|new
name|Delete
argument_list|(
name|row
argument_list|)
decl_stmt|;
name|delete
operator|.
name|addColumn
argument_list|(
name|FAMILY
argument_list|,
name|QUALIFIER
argument_list|)
expr_stmt|;
name|RowMutations
name|mutations
init|=
operator|new
name|RowMutations
argument_list|(
name|row
argument_list|)
decl_stmt|;
name|mutations
operator|.
name|add
argument_list|(
name|put
argument_list|)
expr_stmt|;
name|mutations
operator|.
name|add
argument_list|(
name|delete
argument_list|)
expr_stmt|;
name|Exception
name|exp
decl_stmt|;
name|boolean
name|success
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|50
condition|;
name|i
operator|++
control|)
block|{
name|exp
operator|=
literal|null
expr_stmt|;
name|success
operator|=
literal|false
expr_stmt|;
try|try
block|{
name|table
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
comment|// If at least one operation succeeded, we should have cached the region location.
name|success
operator|=
literal|true
expr_stmt|;
name|table
operator|.
name|get
argument_list|(
name|get
argument_list|)
expr_stmt|;
name|table
operator|.
name|append
argument_list|(
name|append
argument_list|)
expr_stmt|;
name|table
operator|.
name|increment
argument_list|(
name|increment
argument_list|)
expr_stmt|;
name|table
operator|.
name|delete
argument_list|(
name|delete
argument_list|)
expr_stmt|;
name|table
operator|.
name|mutateRow
argument_list|(
name|mutations
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ex
parameter_list|)
block|{
comment|// Only keep track of the last exception that updated the meta cache
if|if
condition|(
name|ClientExceptionsUtil
operator|.
name|isMetaClearingException
argument_list|(
name|ex
argument_list|)
operator|||
name|success
condition|)
block|{
name|exp
operator|=
name|ex
expr_stmt|;
block|}
block|}
comment|// Do not test if we did not touch the meta cache in this iteration.
if|if
condition|(
name|exp
operator|!=
literal|null
operator|&&
name|ClientExceptionsUtil
operator|.
name|isMetaClearingException
argument_list|(
name|exp
argument_list|)
condition|)
block|{
name|assertNull
argument_list|(
name|conn
operator|.
name|getCachedLocation
argument_list|(
name|TABLE_NAME
argument_list|,
name|row
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|success
condition|)
block|{
name|assertNotNull
argument_list|(
name|conn
operator|.
name|getCachedLocation
argument_list|(
name|TABLE_NAME
argument_list|,
name|row
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|public
specifier|static
name|List
argument_list|<
name|Throwable
argument_list|>
name|metaCachePreservingExceptions
parameter_list|()
block|{
return|return
operator|new
name|ArrayList
argument_list|<
name|Throwable
argument_list|>
argument_list|()
block|{
block|{
name|add
argument_list|(
operator|new
name|RegionOpeningException
argument_list|(
literal|" "
argument_list|)
argument_list|)
expr_stmt|;
name|add
argument_list|(
operator|new
name|RegionTooBusyException
argument_list|()
argument_list|)
expr_stmt|;
name|add
argument_list|(
operator|new
name|ThrottlingException
argument_list|(
literal|" "
argument_list|)
argument_list|)
expr_stmt|;
name|add
argument_list|(
operator|new
name|MultiActionResultTooLarge
argument_list|(
literal|" "
argument_list|)
argument_list|)
expr_stmt|;
name|add
argument_list|(
operator|new
name|RetryImmediatelyException
argument_list|(
literal|" "
argument_list|)
argument_list|)
expr_stmt|;
name|add
argument_list|(
operator|new
name|CallQueueTooBigException
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
return|;
block|}
specifier|protected
specifier|static
class|class
name|RegionServerWithFakeRpcServices
extends|extends
name|HRegionServer
block|{
specifier|public
name|RegionServerWithFakeRpcServices
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|CoordinatedStateManager
name|cp
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|super
argument_list|(
name|conf
argument_list|,
name|cp
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|RSRpcServices
name|createRpcServices
parameter_list|()
throws|throws
name|IOException
block|{
return|return
operator|new
name|FakeRSRpcServices
argument_list|(
name|this
argument_list|)
return|;
block|}
block|}
specifier|protected
specifier|static
class|class
name|FakeRSRpcServices
extends|extends
name|RSRpcServices
block|{
specifier|private
name|int
name|numReqs
init|=
operator|-
literal|1
decl_stmt|;
specifier|private
name|int
name|expCount
init|=
operator|-
literal|1
decl_stmt|;
specifier|private
name|List
argument_list|<
name|Throwable
argument_list|>
name|metaCachePreservingExceptions
init|=
name|metaCachePreservingExceptions
argument_list|()
decl_stmt|;
specifier|public
name|FakeRSRpcServices
parameter_list|(
name|HRegionServer
name|rs
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|rs
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|GetResponse
name|get
parameter_list|(
specifier|final
name|RpcController
name|controller
parameter_list|,
specifier|final
name|ClientProtos
operator|.
name|GetRequest
name|request
parameter_list|)
throws|throws
name|ServiceException
block|{
name|throwSomeExceptions
argument_list|()
expr_stmt|;
return|return
name|super
operator|.
name|get
argument_list|(
name|controller
argument_list|,
name|request
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|ClientProtos
operator|.
name|MutateResponse
name|mutate
parameter_list|(
specifier|final
name|RpcController
name|controller
parameter_list|,
specifier|final
name|ClientProtos
operator|.
name|MutateRequest
name|request
parameter_list|)
throws|throws
name|ServiceException
block|{
name|throwSomeExceptions
argument_list|()
expr_stmt|;
return|return
name|super
operator|.
name|mutate
argument_list|(
name|controller
argument_list|,
name|request
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|ClientProtos
operator|.
name|ScanResponse
name|scan
parameter_list|(
specifier|final
name|RpcController
name|controller
parameter_list|,
specifier|final
name|ClientProtos
operator|.
name|ScanRequest
name|request
parameter_list|)
throws|throws
name|ServiceException
block|{
name|throwSomeExceptions
argument_list|()
expr_stmt|;
return|return
name|super
operator|.
name|scan
argument_list|(
name|controller
argument_list|,
name|request
argument_list|)
return|;
block|}
comment|/**      * Throw some exceptions. Mostly throw exceptions which do not clear meta cache.      * Periodically throw NotSevingRegionException which clears the meta cache.      * @throws ServiceException      */
specifier|private
name|void
name|throwSomeExceptions
parameter_list|()
throws|throws
name|ServiceException
block|{
name|numReqs
operator|++
expr_stmt|;
comment|// Succeed every 5 request, throw cache clearing exceptions twice every 5 requests and throw
comment|// meta cache preserving exceptions otherwise.
if|if
condition|(
name|numReqs
operator|%
literal|5
operator|==
literal|0
condition|)
block|{
return|return;
block|}
elseif|else
if|if
condition|(
name|numReqs
operator|%
literal|5
operator|==
literal|1
operator|||
name|numReqs
operator|%
literal|5
operator|==
literal|2
condition|)
block|{
throw|throw
operator|new
name|ServiceException
argument_list|(
operator|new
name|NotServingRegionException
argument_list|()
argument_list|)
throw|;
block|}
comment|// Round robin between different special exceptions.
comment|// This is not ideal since exception types are not tied to the operation performed here,
comment|// But, we don't really care here if we throw MultiActionTooLargeException while doing
comment|// single Gets.
name|expCount
operator|++
expr_stmt|;
name|Throwable
name|t
init|=
name|metaCachePreservingExceptions
operator|.
name|get
argument_list|(
name|expCount
operator|%
name|metaCachePreservingExceptions
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
throw|throw
operator|new
name|ServiceException
argument_list|(
name|t
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

