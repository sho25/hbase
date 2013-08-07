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
name|*
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
name|net
operator|.
name|SocketTimeoutException
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
name|HBaseConfiguration
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
name|HConstants
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
name|HRegionInfo
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
name|HRegionLocation
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
name|ServerName
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
name|ClientService
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
name|ClientService
operator|.
name|BlockingInterface
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
name|RegionServerStoppedException
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
name|Ignore
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

begin_comment
comment|/**  * Test client behavior w/o setting up a cluster.  * Mock up cluster emissions.  */
end_comment

begin_class
specifier|public
class|class
name|TestClientNoCluster
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
name|TestClientNoCluster
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|Configuration
name|conf
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|this
operator|.
name|conf
operator|=
name|HBaseConfiguration
operator|.
name|create
argument_list|()
expr_stmt|;
comment|// Run my HConnection overrides.  Use my little HConnectionImplementation below which
comment|// allows me insert mocks and also use my Registry below rather than the default zk based
comment|// one so tests run faster and don't have zk dependency.
name|this
operator|.
name|conf
operator|.
name|set
argument_list|(
literal|"hbase.client.registry.impl"
argument_list|,
name|SimpleRegistry
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Simple cluster registry inserted in place of our usual zookeeper based one.    */
specifier|static
class|class
name|SimpleRegistry
implements|implements
name|Registry
block|{
specifier|final
name|ServerName
name|META_HOST
init|=
operator|new
name|ServerName
argument_list|(
literal|"10.10.10.10"
argument_list|,
literal|60010
argument_list|,
literal|12345
argument_list|)
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|init
parameter_list|(
name|HConnection
name|connection
parameter_list|)
block|{     }
annotation|@
name|Override
specifier|public
name|HRegionLocation
name|getMetaRegionLocation
parameter_list|()
throws|throws
name|IOException
block|{
return|return
operator|new
name|HRegionLocation
argument_list|(
name|HRegionInfo
operator|.
name|FIRST_META_REGIONINFO
argument_list|,
name|META_HOST
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getClusterId
parameter_list|()
block|{
return|return
name|HConstants
operator|.
name|CLUSTER_ID_DEFAULT
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isTableOnlineState
parameter_list|(
name|byte
index|[]
name|tableName
parameter_list|,
name|boolean
name|enabled
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|enabled
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getCurrentNrHRS
parameter_list|()
throws|throws
name|IOException
block|{
return|return
literal|1
return|;
block|}
block|}
comment|/**    * Remove the @Ignore to try out timeout and retry asettings    * @throws IOException    */
annotation|@
name|Ignore
annotation|@
name|Test
specifier|public
name|void
name|testTimeoutAndRetries
parameter_list|()
throws|throws
name|IOException
block|{
name|Configuration
name|localConfig
init|=
name|HBaseConfiguration
operator|.
name|create
argument_list|(
name|this
operator|.
name|conf
argument_list|)
decl_stmt|;
comment|// This override mocks up our exists/get call to throw a RegionServerStoppedException.
name|localConfig
operator|.
name|set
argument_list|(
literal|"hbase.client.connection.impl"
argument_list|,
name|RpcTimeoutConnection
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|HTable
name|table
init|=
operator|new
name|HTable
argument_list|(
name|localConfig
argument_list|,
name|HConstants
operator|.
name|META_TABLE_NAME
argument_list|)
decl_stmt|;
name|Throwable
name|t
init|=
literal|null
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Start"
argument_list|)
expr_stmt|;
try|try
block|{
comment|// An exists call turns into a get w/ a flag.
name|table
operator|.
name|exists
argument_list|(
operator|new
name|Get
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"abc"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|SocketTimeoutException
name|e
parameter_list|)
block|{
comment|// I expect this exception.
name|LOG
operator|.
name|info
argument_list|(
literal|"Got expected exception"
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|t
operator|=
name|e
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|RetriesExhaustedException
name|e
parameter_list|)
block|{
comment|// This is the old, unwanted behavior.  If we get here FAIL!!!
name|fail
argument_list|()
expr_stmt|;
block|}
finally|finally
block|{
name|table
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Stop"
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|t
operator|!=
literal|null
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test that operation timeout prevails over rpc default timeout and retries, etc.    * @throws IOException    */
annotation|@
name|Test
specifier|public
name|void
name|testRocTimeout
parameter_list|()
throws|throws
name|IOException
block|{
name|Configuration
name|localConfig
init|=
name|HBaseConfiguration
operator|.
name|create
argument_list|(
name|this
operator|.
name|conf
argument_list|)
decl_stmt|;
comment|// This override mocks up our exists/get call to throw a RegionServerStoppedException.
name|localConfig
operator|.
name|set
argument_list|(
literal|"hbase.client.connection.impl"
argument_list|,
name|RpcTimeoutConnection
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|int
name|pause
init|=
literal|10
decl_stmt|;
name|localConfig
operator|.
name|setInt
argument_list|(
literal|"hbase.client.pause"
argument_list|,
name|pause
argument_list|)
expr_stmt|;
name|localConfig
operator|.
name|setInt
argument_list|(
name|HConstants
operator|.
name|HBASE_CLIENT_RETRIES_NUMBER
argument_list|,
literal|10
argument_list|)
expr_stmt|;
comment|// Set the operation timeout to be< the pause.  Expectation is that after first pause, we will
comment|// fail out of the rpc because the rpc timeout will have been set to the operation tiemout
comment|// and it has expired.  Otherwise, if this functionality is broke, all retries will be run --
comment|// all ten of them -- and we'll get the RetriesExhaustedException exception.
name|localConfig
operator|.
name|setInt
argument_list|(
name|HConstants
operator|.
name|HBASE_CLIENT_META_OPERATION_TIMEOUT
argument_list|,
name|pause
operator|-
literal|1
argument_list|)
expr_stmt|;
name|HTable
name|table
init|=
operator|new
name|HTable
argument_list|(
name|localConfig
argument_list|,
name|HConstants
operator|.
name|META_TABLE_NAME
argument_list|)
decl_stmt|;
name|Throwable
name|t
init|=
literal|null
decl_stmt|;
try|try
block|{
comment|// An exists call turns into a get w/ a flag.
name|table
operator|.
name|exists
argument_list|(
operator|new
name|Get
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"abc"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|SocketTimeoutException
name|e
parameter_list|)
block|{
comment|// I expect this exception.
name|LOG
operator|.
name|info
argument_list|(
literal|"Got expected exception"
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|t
operator|=
name|e
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|RetriesExhaustedException
name|e
parameter_list|)
block|{
comment|// This is the old, unwanted behavior.  If we get here FAIL!!!
name|fail
argument_list|()
expr_stmt|;
block|}
finally|finally
block|{
name|table
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
name|assertTrue
argument_list|(
name|t
operator|!=
literal|null
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDoNotRetryMetaScanner
parameter_list|()
throws|throws
name|IOException
block|{
name|this
operator|.
name|conf
operator|.
name|set
argument_list|(
literal|"hbase.client.connection.impl"
argument_list|,
name|RegionServerStoppedOnScannerOpenConnection
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|MetaScanner
operator|.
name|metaScan
argument_list|(
name|this
operator|.
name|conf
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDoNotRetryOnScanNext
parameter_list|()
throws|throws
name|IOException
block|{
name|this
operator|.
name|conf
operator|.
name|set
argument_list|(
literal|"hbase.client.connection.impl"
argument_list|,
name|RegionServerStoppedOnScannerOpenConnection
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
comment|// Go against meta else we will try to find first region for the table on construction which
comment|// means we'll have to do a bunch more mocking.  Tests that go against meta only should be
comment|// good for a bit of testing.
name|HTable
name|table
init|=
operator|new
name|HTable
argument_list|(
name|this
operator|.
name|conf
argument_list|,
name|HConstants
operator|.
name|META_TABLE_NAME
argument_list|)
decl_stmt|;
name|ResultScanner
name|scanner
init|=
name|table
operator|.
name|getScanner
argument_list|(
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|)
decl_stmt|;
try|try
block|{
name|Result
name|result
init|=
literal|null
decl_stmt|;
while|while
condition|(
operator|(
name|result
operator|=
name|scanner
operator|.
name|next
argument_list|()
operator|)
operator|!=
literal|null
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
name|result
argument_list|)
expr_stmt|;
block|}
block|}
finally|finally
block|{
name|scanner
operator|.
name|close
argument_list|()
expr_stmt|;
name|table
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testRegionServerStoppedOnScannerOpen
parameter_list|()
throws|throws
name|IOException
block|{
name|this
operator|.
name|conf
operator|.
name|set
argument_list|(
literal|"hbase.client.connection.impl"
argument_list|,
name|RegionServerStoppedOnScannerOpenConnection
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
comment|// Go against meta else we will try to find first region for the table on construction which
comment|// means we'll have to do a bunch more mocking.  Tests that go against meta only should be
comment|// good for a bit of testing.
name|HTable
name|table
init|=
operator|new
name|HTable
argument_list|(
name|this
operator|.
name|conf
argument_list|,
name|HConstants
operator|.
name|META_TABLE_NAME
argument_list|)
decl_stmt|;
name|ResultScanner
name|scanner
init|=
name|table
operator|.
name|getScanner
argument_list|(
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|)
decl_stmt|;
try|try
block|{
name|Result
name|result
init|=
literal|null
decl_stmt|;
while|while
condition|(
operator|(
name|result
operator|=
name|scanner
operator|.
name|next
argument_list|()
operator|)
operator|!=
literal|null
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
name|result
argument_list|)
expr_stmt|;
block|}
block|}
finally|finally
block|{
name|scanner
operator|.
name|close
argument_list|()
expr_stmt|;
name|table
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * Override to shutdown going to zookeeper for cluster id and meta location.    */
specifier|static
class|class
name|ScanOpenNextThenExceptionThenRecoverConnection
extends|extends
name|HConnectionManager
operator|.
name|HConnectionImplementation
block|{
specifier|final
name|ClientService
operator|.
name|BlockingInterface
name|stub
decl_stmt|;
name|ScanOpenNextThenExceptionThenRecoverConnection
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|boolean
name|managed
parameter_list|,
name|ExecutorService
name|pool
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|conf
argument_list|,
name|managed
argument_list|)
expr_stmt|;
comment|// Mock up my stub so open scanner returns a scanner id and then on next, we throw
comment|// exceptions for three times and then after that, we return no more to scan.
name|this
operator|.
name|stub
operator|=
name|Mockito
operator|.
name|mock
argument_list|(
name|ClientService
operator|.
name|BlockingInterface
operator|.
name|class
argument_list|)
expr_stmt|;
name|long
name|sid
init|=
literal|12345L
decl_stmt|;
try|try
block|{
name|Mockito
operator|.
name|when
argument_list|(
name|stub
operator|.
name|scan
argument_list|(
operator|(
name|RpcController
operator|)
name|Mockito
operator|.
name|any
argument_list|()
argument_list|,
operator|(
name|ClientProtos
operator|.
name|ScanRequest
operator|)
name|Mockito
operator|.
name|any
argument_list|()
argument_list|)
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|ClientProtos
operator|.
name|ScanResponse
operator|.
name|newBuilder
argument_list|()
operator|.
name|setScannerId
argument_list|(
name|sid
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|thenThrow
argument_list|(
operator|new
name|ServiceException
argument_list|(
operator|new
name|RegionServerStoppedException
argument_list|(
literal|"From Mockito"
argument_list|)
argument_list|)
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|ClientProtos
operator|.
name|ScanResponse
operator|.
name|newBuilder
argument_list|()
operator|.
name|setScannerId
argument_list|(
name|sid
argument_list|)
operator|.
name|setMoreResults
argument_list|(
literal|false
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ServiceException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|BlockingInterface
name|getClient
parameter_list|(
name|ServerName
name|sn
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|this
operator|.
name|stub
return|;
block|}
block|}
comment|/**    * Override to shutdown going to zookeeper for cluster id and meta location.    */
specifier|static
class|class
name|RegionServerStoppedOnScannerOpenConnection
extends|extends
name|HConnectionManager
operator|.
name|HConnectionImplementation
block|{
specifier|final
name|ClientService
operator|.
name|BlockingInterface
name|stub
decl_stmt|;
name|RegionServerStoppedOnScannerOpenConnection
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|boolean
name|managed
parameter_list|,
name|ExecutorService
name|pool
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|conf
argument_list|,
name|managed
argument_list|)
expr_stmt|;
comment|// Mock up my stub so open scanner returns a scanner id and then on next, we throw
comment|// exceptions for three times and then after that, we return no more to scan.
name|this
operator|.
name|stub
operator|=
name|Mockito
operator|.
name|mock
argument_list|(
name|ClientService
operator|.
name|BlockingInterface
operator|.
name|class
argument_list|)
expr_stmt|;
name|long
name|sid
init|=
literal|12345L
decl_stmt|;
try|try
block|{
name|Mockito
operator|.
name|when
argument_list|(
name|stub
operator|.
name|scan
argument_list|(
operator|(
name|RpcController
operator|)
name|Mockito
operator|.
name|any
argument_list|()
argument_list|,
operator|(
name|ClientProtos
operator|.
name|ScanRequest
operator|)
name|Mockito
operator|.
name|any
argument_list|()
argument_list|)
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|ClientProtos
operator|.
name|ScanResponse
operator|.
name|newBuilder
argument_list|()
operator|.
name|setScannerId
argument_list|(
name|sid
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|thenThrow
argument_list|(
operator|new
name|ServiceException
argument_list|(
operator|new
name|RegionServerStoppedException
argument_list|(
literal|"From Mockito"
argument_list|)
argument_list|)
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|ClientProtos
operator|.
name|ScanResponse
operator|.
name|newBuilder
argument_list|()
operator|.
name|setScannerId
argument_list|(
name|sid
argument_list|)
operator|.
name|setMoreResults
argument_list|(
literal|false
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ServiceException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|BlockingInterface
name|getClient
parameter_list|(
name|ServerName
name|sn
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|this
operator|.
name|stub
return|;
block|}
block|}
comment|/**    * Override to check we are setting rpc timeout right.    */
specifier|static
class|class
name|RpcTimeoutConnection
extends|extends
name|HConnectionManager
operator|.
name|HConnectionImplementation
block|{
specifier|final
name|ClientService
operator|.
name|BlockingInterface
name|stub
decl_stmt|;
name|RpcTimeoutConnection
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|boolean
name|managed
parameter_list|,
name|ExecutorService
name|pool
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|conf
argument_list|,
name|managed
argument_list|)
expr_stmt|;
comment|// Mock up my stub so an exists call -- which turns into a get -- throws an exception
name|this
operator|.
name|stub
operator|=
name|Mockito
operator|.
name|mock
argument_list|(
name|ClientService
operator|.
name|BlockingInterface
operator|.
name|class
argument_list|)
expr_stmt|;
try|try
block|{
name|Mockito
operator|.
name|when
argument_list|(
name|stub
operator|.
name|get
argument_list|(
operator|(
name|RpcController
operator|)
name|Mockito
operator|.
name|any
argument_list|()
argument_list|,
operator|(
name|ClientProtos
operator|.
name|GetRequest
operator|)
name|Mockito
operator|.
name|any
argument_list|()
argument_list|)
argument_list|)
operator|.
name|thenThrow
argument_list|(
operator|new
name|ServiceException
argument_list|(
operator|new
name|RegionServerStoppedException
argument_list|(
literal|"From Mockito"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ServiceException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|BlockingInterface
name|getClient
parameter_list|(
name|ServerName
name|sn
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|this
operator|.
name|stub
return|;
block|}
block|}
block|}
end_class

end_unit

