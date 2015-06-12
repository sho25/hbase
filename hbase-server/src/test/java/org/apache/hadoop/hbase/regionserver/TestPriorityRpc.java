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
name|regionserver
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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|security
operator|.
name|Superusers
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
name|security
operator|.
name|User
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
name|testclassification
operator|.
name|RegionServerTests
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
name|ByteStringer
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
name|CoordinatedStateManagerFactory
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
name|CoordinatedStateManager
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
name|PriorityFunction
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
name|Get
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
name|GetRequest
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
name|ScanRequest
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
name|HBaseProtos
operator|.
name|RegionSpecifier
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
name|HBaseProtos
operator|.
name|RegionSpecifier
operator|.
name|RegionSpecifierType
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
name|RPCProtos
operator|.
name|RequestHeader
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
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|ByteString
import|;
end_import

begin_comment
comment|/**  * Tests that verify certain RPCs get a higher QoS.  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|RegionServerTests
operator|.
name|class
block|,
name|MediumTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestPriorityRpc
block|{
specifier|private
name|HRegionServer
name|regionServer
init|=
literal|null
decl_stmt|;
specifier|private
name|PriorityFunction
name|priority
init|=
literal|null
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|setup
parameter_list|()
block|{
name|Configuration
name|conf
init|=
name|HBaseConfiguration
operator|.
name|create
argument_list|()
decl_stmt|;
name|conf
operator|.
name|setBoolean
argument_list|(
literal|"hbase.testing.nocluster"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
comment|// No need to do ZK
name|CoordinatedStateManager
name|cp
init|=
name|CoordinatedStateManagerFactory
operator|.
name|getCoordinatedStateManager
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|regionServer
operator|=
name|HRegionServer
operator|.
name|constructRegionServer
argument_list|(
name|HRegionServer
operator|.
name|class
argument_list|,
name|conf
argument_list|,
name|cp
argument_list|)
expr_stmt|;
name|priority
operator|=
name|regionServer
operator|.
name|rpcServices
operator|.
name|getPriority
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testQosFunctionForMeta
parameter_list|()
throws|throws
name|IOException
block|{
name|priority
operator|=
name|regionServer
operator|.
name|rpcServices
operator|.
name|getPriority
argument_list|()
expr_stmt|;
name|RequestHeader
operator|.
name|Builder
name|headerBuilder
init|=
name|RequestHeader
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
comment|//create a rpc request that has references to hbase:meta region and also
comment|//uses one of the known argument classes (known argument classes are
comment|//listed in HRegionServer.QosFunctionImpl.knownArgumentClasses)
name|headerBuilder
operator|.
name|setMethodName
argument_list|(
literal|"foo"
argument_list|)
expr_stmt|;
name|GetRequest
operator|.
name|Builder
name|getRequestBuilder
init|=
name|GetRequest
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
name|RegionSpecifier
operator|.
name|Builder
name|regionSpecifierBuilder
init|=
name|RegionSpecifier
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
name|regionSpecifierBuilder
operator|.
name|setType
argument_list|(
name|RegionSpecifierType
operator|.
name|REGION_NAME
argument_list|)
expr_stmt|;
name|ByteString
name|name
init|=
name|ByteStringer
operator|.
name|wrap
argument_list|(
name|HRegionInfo
operator|.
name|FIRST_META_REGIONINFO
operator|.
name|getRegionName
argument_list|()
argument_list|)
decl_stmt|;
name|regionSpecifierBuilder
operator|.
name|setValue
argument_list|(
name|name
argument_list|)
expr_stmt|;
name|RegionSpecifier
name|regionSpecifier
init|=
name|regionSpecifierBuilder
operator|.
name|build
argument_list|()
decl_stmt|;
name|getRequestBuilder
operator|.
name|setRegion
argument_list|(
name|regionSpecifier
argument_list|)
expr_stmt|;
name|Get
operator|.
name|Builder
name|getBuilder
init|=
name|Get
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
name|getBuilder
operator|.
name|setRow
argument_list|(
name|ByteStringer
operator|.
name|wrap
argument_list|(
literal|"somerow"
operator|.
name|getBytes
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|getRequestBuilder
operator|.
name|setGet
argument_list|(
name|getBuilder
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|GetRequest
name|getRequest
init|=
name|getRequestBuilder
operator|.
name|build
argument_list|()
decl_stmt|;
name|RequestHeader
name|header
init|=
name|headerBuilder
operator|.
name|build
argument_list|()
decl_stmt|;
name|HRegion
name|mockRegion
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|HRegion
operator|.
name|class
argument_list|)
decl_stmt|;
name|HRegionServer
name|mockRS
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|HRegionServer
operator|.
name|class
argument_list|)
decl_stmt|;
name|RSRpcServices
name|mockRpc
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|RSRpcServices
operator|.
name|class
argument_list|)
decl_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|mockRS
operator|.
name|getRSRpcServices
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|mockRpc
argument_list|)
expr_stmt|;
name|HRegionInfo
name|mockRegionInfo
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
name|mockRpc
operator|.
name|getRegion
argument_list|(
operator|(
name|RegionSpecifier
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
name|mockRegion
argument_list|)
expr_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|mockRegion
operator|.
name|getRegionInfo
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|mockRegionInfo
argument_list|)
expr_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|mockRegionInfo
operator|.
name|isSystemTable
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
literal|true
argument_list|)
expr_stmt|;
comment|// Presume type.
operator|(
operator|(
name|AnnotationReadingPriorityFunction
operator|)
name|priority
operator|)
operator|.
name|setRegionServer
argument_list|(
name|mockRS
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|HConstants
operator|.
name|SYSTEMTABLE_QOS
argument_list|,
name|priority
operator|.
name|getPriority
argument_list|(
name|header
argument_list|,
name|getRequest
argument_list|,
name|User
operator|.
name|createUserForTesting
argument_list|(
name|regionServer
operator|.
name|conf
argument_list|,
literal|"someuser"
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"somegroup"
block|}
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testQosFunctionWithoutKnownArgument
parameter_list|()
throws|throws
name|IOException
block|{
comment|//The request is not using any of the
comment|//known argument classes (it uses one random request class)
comment|//(known argument classes are listed in
comment|//HRegionServer.QosFunctionImpl.knownArgumentClasses)
name|RequestHeader
operator|.
name|Builder
name|headerBuilder
init|=
name|RequestHeader
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
name|headerBuilder
operator|.
name|setMethodName
argument_list|(
literal|"foo"
argument_list|)
expr_stmt|;
name|RequestHeader
name|header
init|=
name|headerBuilder
operator|.
name|build
argument_list|()
decl_stmt|;
name|PriorityFunction
name|qosFunc
init|=
name|regionServer
operator|.
name|rpcServices
operator|.
name|getPriority
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|HConstants
operator|.
name|NORMAL_QOS
argument_list|,
name|qosFunc
operator|.
name|getPriority
argument_list|(
name|header
argument_list|,
literal|null
argument_list|,
name|User
operator|.
name|createUserForTesting
argument_list|(
name|regionServer
operator|.
name|conf
argument_list|,
literal|"someuser"
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"somegroup"
block|}
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testQosFunctionForRequestCalledBySuperUser
parameter_list|()
throws|throws
name|Exception
block|{
name|RequestHeader
operator|.
name|Builder
name|headerBuilder
init|=
name|RequestHeader
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
name|headerBuilder
operator|.
name|setMethodName
argument_list|(
literal|"foo"
argument_list|)
expr_stmt|;
name|RequestHeader
name|header
init|=
name|headerBuilder
operator|.
name|build
argument_list|()
decl_stmt|;
name|PriorityFunction
name|qosFunc
init|=
name|regionServer
operator|.
name|rpcServices
operator|.
name|getPriority
argument_list|()
decl_stmt|;
comment|//test superusers
name|regionServer
operator|.
name|conf
operator|.
name|set
argument_list|(
name|Superusers
operator|.
name|SUPERUSER_CONF_KEY
argument_list|,
literal|"samplesuperuser"
argument_list|)
expr_stmt|;
name|Superusers
operator|.
name|initialize
argument_list|(
name|regionServer
operator|.
name|conf
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|HConstants
operator|.
name|ADMIN_QOS
argument_list|,
name|qosFunc
operator|.
name|getPriority
argument_list|(
name|header
argument_list|,
literal|null
argument_list|,
name|User
operator|.
name|createUserForTesting
argument_list|(
name|regionServer
operator|.
name|conf
argument_list|,
literal|"samplesuperuser"
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"somegroup"
block|}
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
comment|//test supergroups
name|regionServer
operator|.
name|conf
operator|.
name|set
argument_list|(
name|Superusers
operator|.
name|SUPERUSER_CONF_KEY
argument_list|,
literal|"@samplesupergroup"
argument_list|)
expr_stmt|;
name|Superusers
operator|.
name|initialize
argument_list|(
name|regionServer
operator|.
name|conf
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|HConstants
operator|.
name|ADMIN_QOS
argument_list|,
name|qosFunc
operator|.
name|getPriority
argument_list|(
name|header
argument_list|,
literal|null
argument_list|,
name|User
operator|.
name|createUserForTesting
argument_list|(
name|regionServer
operator|.
name|conf
argument_list|,
literal|"regularuser"
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"samplesupergroup"
block|}
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testQosFunctionForScanMethod
parameter_list|()
throws|throws
name|IOException
block|{
name|RequestHeader
operator|.
name|Builder
name|headerBuilder
init|=
name|RequestHeader
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
name|headerBuilder
operator|.
name|setMethodName
argument_list|(
literal|"Scan"
argument_list|)
expr_stmt|;
name|RequestHeader
name|header
init|=
name|headerBuilder
operator|.
name|build
argument_list|()
decl_stmt|;
comment|//build an empty scan request
name|ScanRequest
operator|.
name|Builder
name|scanBuilder
init|=
name|ScanRequest
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
name|ScanRequest
name|scanRequest
init|=
name|scanBuilder
operator|.
name|build
argument_list|()
decl_stmt|;
name|HRegion
name|mockRegion
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|HRegion
operator|.
name|class
argument_list|)
decl_stmt|;
name|HRegionServer
name|mockRS
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|HRegionServer
operator|.
name|class
argument_list|)
decl_stmt|;
name|RSRpcServices
name|mockRpc
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|RSRpcServices
operator|.
name|class
argument_list|)
decl_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|mockRS
operator|.
name|getRSRpcServices
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|mockRpc
argument_list|)
expr_stmt|;
name|HRegionInfo
name|mockRegionInfo
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
name|mockRpc
operator|.
name|getRegion
argument_list|(
operator|(
name|RegionSpecifier
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
name|mockRegion
argument_list|)
expr_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|mockRegion
operator|.
name|getRegionInfo
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|mockRegionInfo
argument_list|)
expr_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|mockRegionInfo
operator|.
name|isSystemTable
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
literal|false
argument_list|)
expr_stmt|;
comment|// Presume type.
operator|(
operator|(
name|AnnotationReadingPriorityFunction
operator|)
name|priority
operator|)
operator|.
name|setRegionServer
argument_list|(
name|mockRS
argument_list|)
expr_stmt|;
name|int
name|qos
init|=
name|priority
operator|.
name|getPriority
argument_list|(
name|header
argument_list|,
name|scanRequest
argument_list|,
name|User
operator|.
name|createUserForTesting
argument_list|(
name|regionServer
operator|.
name|conf
argument_list|,
literal|"someuser"
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"somegroup"
block|}
argument_list|)
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
literal|""
operator|+
name|qos
argument_list|,
name|qos
operator|==
name|HConstants
operator|.
name|NORMAL_QOS
argument_list|)
expr_stmt|;
comment|//build a scan request with scannerID
name|scanBuilder
operator|=
name|ScanRequest
operator|.
name|newBuilder
argument_list|()
expr_stmt|;
name|scanBuilder
operator|.
name|setScannerId
argument_list|(
literal|12345
argument_list|)
expr_stmt|;
name|scanRequest
operator|=
name|scanBuilder
operator|.
name|build
argument_list|()
expr_stmt|;
comment|//mock out a high priority type handling and see the QoS returned
name|RegionScanner
name|mockRegionScanner
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|RegionScanner
operator|.
name|class
argument_list|)
decl_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|mockRpc
operator|.
name|getScanner
argument_list|(
literal|12345
argument_list|)
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|mockRegionScanner
argument_list|)
expr_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|mockRegionScanner
operator|.
name|getRegionInfo
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|mockRegionInfo
argument_list|)
expr_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|mockRpc
operator|.
name|getRegion
argument_list|(
operator|(
name|RegionSpecifier
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
name|mockRegion
argument_list|)
expr_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|mockRegion
operator|.
name|getRegionInfo
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|mockRegionInfo
argument_list|)
expr_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|mockRegionInfo
operator|.
name|isSystemTable
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
literal|true
argument_list|)
expr_stmt|;
comment|// Presume type.
operator|(
operator|(
name|AnnotationReadingPriorityFunction
operator|)
name|priority
operator|)
operator|.
name|setRegionServer
argument_list|(
name|mockRS
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|HConstants
operator|.
name|SYSTEMTABLE_QOS
argument_list|,
name|priority
operator|.
name|getPriority
argument_list|(
name|header
argument_list|,
name|scanRequest
argument_list|,
name|User
operator|.
name|createUserForTesting
argument_list|(
name|regionServer
operator|.
name|conf
argument_list|,
literal|"someuser"
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"somegroup"
block|}
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
comment|//the same as above but with non-meta region
name|Mockito
operator|.
name|when
argument_list|(
name|mockRegionInfo
operator|.
name|isSystemTable
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|HConstants
operator|.
name|NORMAL_QOS
argument_list|,
name|priority
operator|.
name|getPriority
argument_list|(
name|header
argument_list|,
name|scanRequest
argument_list|,
name|User
operator|.
name|createUserForTesting
argument_list|(
name|regionServer
operator|.
name|conf
argument_list|,
literal|"someuser"
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"somegroup"
block|}
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

