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
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|HBaseTestingUtility
operator|.
name|fam1
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
name|collect
operator|.
name|Lists
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
name|List
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
name|atomic
operator|.
name|AtomicInteger
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
name|CellScannable
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
name|CellScanner
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
name|HBaseTestingUtility
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
name|TableName
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
name|coprocessor
operator|.
name|CoprocessorHost
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
name|coprocessor
operator|.
name|ProtobufCoprocessorService
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
name|DelegatingHBaseRpcController
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
name|HBaseRpcController
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
name|AfterClass
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
name|Rule
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
name|junit
operator|.
name|rules
operator|.
name|TestName
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
name|TestRpcControllerFactory
block|{
specifier|public
specifier|static
class|class
name|StaticRpcControllerFactory
extends|extends
name|RpcControllerFactory
block|{
specifier|public
name|StaticRpcControllerFactory
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|super
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|HBaseRpcController
name|newController
parameter_list|()
block|{
return|return
operator|new
name|CountingRpcController
argument_list|(
name|super
operator|.
name|newController
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|HBaseRpcController
name|newController
parameter_list|(
specifier|final
name|CellScanner
name|cellScanner
parameter_list|)
block|{
return|return
operator|new
name|CountingRpcController
argument_list|(
name|super
operator|.
name|newController
argument_list|(
name|cellScanner
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|HBaseRpcController
name|newController
parameter_list|(
specifier|final
name|List
argument_list|<
name|CellScannable
argument_list|>
name|cellIterables
parameter_list|)
block|{
return|return
operator|new
name|CountingRpcController
argument_list|(
name|super
operator|.
name|newController
argument_list|(
name|cellIterables
argument_list|)
argument_list|)
return|;
block|}
block|}
specifier|public
specifier|static
class|class
name|CountingRpcController
extends|extends
name|DelegatingHBaseRpcController
block|{
specifier|private
specifier|static
name|AtomicInteger
name|INT_PRIORITY
init|=
operator|new
name|AtomicInteger
argument_list|()
decl_stmt|;
specifier|private
specifier|static
name|AtomicInteger
name|TABLE_PRIORITY
init|=
operator|new
name|AtomicInteger
argument_list|()
decl_stmt|;
specifier|public
name|CountingRpcController
parameter_list|(
name|HBaseRpcController
name|delegate
parameter_list|)
block|{
name|super
argument_list|(
name|delegate
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setPriority
parameter_list|(
name|int
name|priority
parameter_list|)
block|{
name|super
operator|.
name|setPriority
argument_list|(
name|priority
argument_list|)
expr_stmt|;
name|INT_PRIORITY
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setPriority
parameter_list|(
name|TableName
name|tn
parameter_list|)
block|{
name|super
operator|.
name|setPriority
argument_list|(
name|tn
argument_list|)
expr_stmt|;
comment|// ignore counts for system tables - it could change and we really only want to check on what
comment|// the client should change
if|if
condition|(
name|tn
operator|!=
literal|null
operator|&&
operator|!
name|tn
operator|.
name|isSystemTable
argument_list|()
condition|)
block|{
name|TABLE_PRIORITY
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
block|}
block|}
block|}
specifier|private
specifier|static
specifier|final
name|HBaseTestingUtility
name|UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
annotation|@
name|Rule
specifier|public
name|TestName
name|name
init|=
operator|new
name|TestName
argument_list|()
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|setup
parameter_list|()
throws|throws
name|Exception
block|{
comment|// load an endpoint so we have an endpoint to test - it doesn't matter which one, but
comment|// this is already in tests, so we can just use it.
name|Configuration
name|conf
init|=
name|UTIL
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|CoprocessorHost
operator|.
name|REGION_COPROCESSOR_CONF_KEY
argument_list|,
name|ProtobufCoprocessorService
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|startMiniCluster
argument_list|()
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|teardown
parameter_list|()
throws|throws
name|Exception
block|{
name|UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
comment|/**    * check some of the methods and make sure we are incrementing each time. Its a bit tediuous to    * cover all methods here and really is a bit brittle since we can always add new methods but    * won't be sure to add them here. So we just can cover the major ones.    * @throws Exception on failure    */
annotation|@
name|Test
specifier|public
name|void
name|testCountController
parameter_list|()
throws|throws
name|Exception
block|{
name|Configuration
name|conf
init|=
operator|new
name|Configuration
argument_list|(
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
comment|// setup our custom controller
name|conf
operator|.
name|set
argument_list|(
name|RpcControllerFactory
operator|.
name|CUSTOM_CONTROLLER_CONF_KEY
argument_list|,
name|StaticRpcControllerFactory
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
specifier|final
name|TableName
name|tableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|name
operator|.
name|getMethodName
argument_list|()
argument_list|)
decl_stmt|;
name|UTIL
operator|.
name|createTable
argument_list|(
name|tableName
argument_list|,
name|fam1
argument_list|)
operator|.
name|close
argument_list|()
expr_stmt|;
comment|// change one of the connection properties so we get a new Connection with our configuration
name|conf
operator|.
name|setInt
argument_list|(
name|HConstants
operator|.
name|HBASE_RPC_TIMEOUT_KEY
argument_list|,
name|HConstants
operator|.
name|DEFAULT_HBASE_RPC_TIMEOUT
operator|+
literal|1
argument_list|)
expr_stmt|;
name|Connection
name|connection
init|=
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|Table
name|table
init|=
name|connection
operator|.
name|getTable
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|byte
index|[]
name|row
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row"
argument_list|)
decl_stmt|;
name|Put
name|p
init|=
operator|new
name|Put
argument_list|(
name|row
argument_list|)
decl_stmt|;
name|p
operator|.
name|addColumn
argument_list|(
name|fam1
argument_list|,
name|fam1
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"val0"
argument_list|)
argument_list|)
expr_stmt|;
name|table
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
name|Integer
name|counter
init|=
literal|1
decl_stmt|;
name|counter
operator|=
name|verifyCount
argument_list|(
name|counter
argument_list|)
expr_stmt|;
name|Delete
name|d
init|=
operator|new
name|Delete
argument_list|(
name|row
argument_list|)
decl_stmt|;
name|d
operator|.
name|addColumn
argument_list|(
name|fam1
argument_list|,
name|fam1
argument_list|)
expr_stmt|;
name|table
operator|.
name|delete
argument_list|(
name|d
argument_list|)
expr_stmt|;
name|counter
operator|=
name|verifyCount
argument_list|(
name|counter
argument_list|)
expr_stmt|;
name|Put
name|p2
init|=
operator|new
name|Put
argument_list|(
name|row
argument_list|)
decl_stmt|;
name|p2
operator|.
name|addColumn
argument_list|(
name|fam1
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"qual"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"val1"
argument_list|)
argument_list|)
expr_stmt|;
name|table
operator|.
name|batch
argument_list|(
name|Lists
operator|.
name|newArrayList
argument_list|(
name|p
argument_list|,
name|p2
argument_list|)
argument_list|,
literal|null
argument_list|)
expr_stmt|;
comment|// this only goes to a single server, so we don't need to change the count here
name|counter
operator|=
name|verifyCount
argument_list|(
name|counter
argument_list|)
expr_stmt|;
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
name|fam1
argument_list|,
name|fam1
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"val2"
argument_list|)
argument_list|)
expr_stmt|;
name|table
operator|.
name|append
argument_list|(
name|append
argument_list|)
expr_stmt|;
name|counter
operator|=
name|verifyCount
argument_list|(
name|counter
argument_list|)
expr_stmt|;
comment|// and check the major lookup calls as well
name|Get
name|g
init|=
operator|new
name|Get
argument_list|(
name|row
argument_list|)
decl_stmt|;
name|table
operator|.
name|get
argument_list|(
name|g
argument_list|)
expr_stmt|;
name|counter
operator|=
name|verifyCount
argument_list|(
name|counter
argument_list|)
expr_stmt|;
name|ResultScanner
name|scan
init|=
name|table
operator|.
name|getScanner
argument_list|(
name|fam1
argument_list|)
decl_stmt|;
name|scan
operator|.
name|next
argument_list|()
expr_stmt|;
name|scan
operator|.
name|close
argument_list|()
expr_stmt|;
name|counter
operator|=
name|verifyCount
argument_list|(
name|counter
operator|+
literal|1
argument_list|)
expr_stmt|;
name|Get
name|g2
init|=
operator|new
name|Get
argument_list|(
name|row
argument_list|)
decl_stmt|;
name|table
operator|.
name|get
argument_list|(
name|Lists
operator|.
name|newArrayList
argument_list|(
name|g
argument_list|,
name|g2
argument_list|)
argument_list|)
expr_stmt|;
comment|// same server, so same as above for not changing count
name|counter
operator|=
name|verifyCount
argument_list|(
name|counter
argument_list|)
expr_stmt|;
comment|// make sure all the scanner types are covered
name|Scan
name|scanInfo
init|=
operator|new
name|Scan
argument_list|(
name|row
argument_list|)
decl_stmt|;
comment|// regular small
name|scanInfo
operator|.
name|setSmall
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|counter
operator|=
name|doScan
argument_list|(
name|table
argument_list|,
name|scanInfo
argument_list|,
name|counter
argument_list|)
expr_stmt|;
comment|// reversed, small
name|scanInfo
operator|.
name|setReversed
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|counter
operator|=
name|doScan
argument_list|(
name|table
argument_list|,
name|scanInfo
argument_list|,
name|counter
argument_list|)
expr_stmt|;
comment|// reversed, regular
name|scanInfo
operator|.
name|setSmall
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|counter
operator|=
name|doScan
argument_list|(
name|table
argument_list|,
name|scanInfo
argument_list|,
name|counter
operator|+
literal|1
argument_list|)
expr_stmt|;
name|table
operator|.
name|close
argument_list|()
expr_stmt|;
name|connection
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
name|int
name|doScan
parameter_list|(
name|Table
name|table
parameter_list|,
name|Scan
name|scan
parameter_list|,
name|int
name|expectedCount
parameter_list|)
throws|throws
name|IOException
block|{
name|ResultScanner
name|results
init|=
name|table
operator|.
name|getScanner
argument_list|(
name|scan
argument_list|)
decl_stmt|;
name|results
operator|.
name|next
argument_list|()
expr_stmt|;
name|results
operator|.
name|close
argument_list|()
expr_stmt|;
return|return
name|verifyCount
argument_list|(
name|expectedCount
argument_list|)
return|;
block|}
name|int
name|verifyCount
parameter_list|(
name|Integer
name|counter
parameter_list|)
block|{
name|assertTrue
argument_list|(
name|CountingRpcController
operator|.
name|TABLE_PRIORITY
operator|.
name|get
argument_list|()
operator|>=
name|counter
operator|.
name|intValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|CountingRpcController
operator|.
name|INT_PRIORITY
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|CountingRpcController
operator|.
name|TABLE_PRIORITY
operator|.
name|get
argument_list|()
operator|+
literal|1
return|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testFallbackToDefaultRpcControllerFactory
parameter_list|()
block|{
name|Configuration
name|conf
init|=
operator|new
name|Configuration
argument_list|(
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|RpcControllerFactory
operator|.
name|CUSTOM_CONTROLLER_CONF_KEY
argument_list|,
literal|"foo.bar.Baz"
argument_list|)
expr_stmt|;
comment|// Should not fail
name|RpcControllerFactory
name|factory
init|=
name|RpcControllerFactory
operator|.
name|instantiate
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
name|factory
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|factory
operator|.
name|getClass
argument_list|()
argument_list|,
name|RpcControllerFactory
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

