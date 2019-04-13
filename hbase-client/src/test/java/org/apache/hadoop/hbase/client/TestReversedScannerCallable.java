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
name|HBaseClassTestRule
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
name|RegionLocations
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
name|SmallTests
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
name|ClassRule
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
name|runner
operator|.
name|RunWith
import|;
end_import

begin_import
import|import
name|org
operator|.
name|mockito
operator|.
name|Mock
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
name|runners
operator|.
name|MockitoJUnitRunner
import|;
end_import

begin_class
annotation|@
name|RunWith
argument_list|(
name|MockitoJUnitRunner
operator|.
name|class
argument_list|)
annotation|@
name|Category
argument_list|(
block|{
name|ClientTests
operator|.
name|class
block|,
name|SmallTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestReversedScannerCallable
block|{
annotation|@
name|ClassRule
specifier|public
specifier|static
specifier|final
name|HBaseClassTestRule
name|CLASS_RULE
init|=
name|HBaseClassTestRule
operator|.
name|forClass
argument_list|(
name|TestReversedScannerCallable
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|Mock
specifier|private
name|ClusterConnection
name|connection
decl_stmt|;
annotation|@
name|Mock
specifier|private
name|Scan
name|scan
decl_stmt|;
annotation|@
name|Mock
specifier|private
name|RpcControllerFactory
name|rpcFactory
decl_stmt|;
annotation|@
name|Mock
specifier|private
name|RegionLocations
name|regionLocations
decl_stmt|;
specifier|private
specifier|final
name|byte
index|[]
name|ROW
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row1"
argument_list|)
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
name|byte
index|[]
name|ROW_BEFORE
init|=
name|ConnectionUtils
operator|.
name|createCloseRowBefore
argument_list|(
name|ROW
argument_list|)
decl_stmt|;
name|Configuration
name|conf
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|Configuration
operator|.
name|class
argument_list|)
decl_stmt|;
name|HRegionLocation
name|regionLocation
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|HRegionLocation
operator|.
name|class
argument_list|)
decl_stmt|;
name|ServerName
name|serverName
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|ServerName
operator|.
name|class
argument_list|)
decl_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|connection
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
name|Mockito
operator|.
name|when
argument_list|(
name|regionLocations
operator|.
name|size
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|regionLocations
operator|.
name|getRegionLocation
argument_list|(
literal|0
argument_list|)
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|regionLocation
argument_list|)
expr_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|regionLocation
operator|.
name|getHostname
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
literal|"localhost"
argument_list|)
expr_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|regionLocation
operator|.
name|getServerName
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|serverName
argument_list|)
expr_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|scan
operator|.
name|includeStartRow
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|scan
operator|.
name|getStartRow
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|ROW
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testPrepareDoesNotUseCache
parameter_list|()
throws|throws
name|Exception
block|{
name|TableName
name|tableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"MyTable"
argument_list|)
decl_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|connection
operator|.
name|relocateRegion
argument_list|(
name|tableName
argument_list|,
name|ROW
argument_list|,
literal|0
argument_list|)
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|regionLocations
argument_list|)
expr_stmt|;
name|ReversedScannerCallable
name|callable
init|=
operator|new
name|ReversedScannerCallable
argument_list|(
name|connection
argument_list|,
name|tableName
argument_list|,
name|scan
argument_list|,
literal|null
argument_list|,
name|rpcFactory
argument_list|)
decl_stmt|;
name|callable
operator|.
name|prepare
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|Mockito
operator|.
name|verify
argument_list|(
name|connection
argument_list|)
operator|.
name|relocateRegion
argument_list|(
name|tableName
argument_list|,
name|ROW
argument_list|,
literal|0
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testPrepareUsesCache
parameter_list|()
throws|throws
name|Exception
block|{
name|TableName
name|tableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"MyTable"
argument_list|)
decl_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|connection
operator|.
name|locateRegion
argument_list|(
name|tableName
argument_list|,
name|ROW
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|,
literal|0
argument_list|)
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|regionLocations
argument_list|)
expr_stmt|;
name|ReversedScannerCallable
name|callable
init|=
operator|new
name|ReversedScannerCallable
argument_list|(
name|connection
argument_list|,
name|tableName
argument_list|,
name|scan
argument_list|,
literal|null
argument_list|,
name|rpcFactory
argument_list|)
decl_stmt|;
name|callable
operator|.
name|prepare
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|Mockito
operator|.
name|verify
argument_list|(
name|connection
argument_list|)
operator|.
name|locateRegion
argument_list|(
name|tableName
argument_list|,
name|ROW
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|,
literal|0
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

