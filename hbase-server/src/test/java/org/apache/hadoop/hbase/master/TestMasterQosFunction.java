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
name|master
package|;
end_package

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|when
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
name|QosTestHelper
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
name|regionserver
operator|.
name|AnnotationReadingPriorityFunction
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
name|MasterTests
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
name|mockito
operator|.
name|Mockito
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
name|shaded
operator|.
name|protobuf
operator|.
name|ProtobufUtil
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
name|shaded
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|HBaseProtos
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
name|shaded
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|RegionServerStatusProtos
import|;
end_import

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|MasterTests
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
name|TestMasterQosFunction
extends|extends
name|QosTestHelper
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
name|TestMasterQosFunction
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|Configuration
name|conf
decl_stmt|;
specifier|private
name|RSRpcServices
name|rpcServices
decl_stmt|;
specifier|private
name|AnnotationReadingPriorityFunction
name|qosFunction
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
block|{
name|conf
operator|=
name|HBaseConfiguration
operator|.
name|create
argument_list|()
expr_stmt|;
name|rpcServices
operator|=
name|Mockito
operator|.
name|mock
argument_list|(
name|MasterRpcServices
operator|.
name|class
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|rpcServices
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
name|qosFunction
operator|=
operator|new
name|MasterAnnotationReadingPriorityFunction
argument_list|(
name|rpcServices
argument_list|,
name|MasterRpcServices
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testRegionInTransition
parameter_list|()
throws|throws
name|IOException
block|{
comment|// Check ReportRegionInTransition
name|HBaseProtos
operator|.
name|RegionInfo
name|meta_ri
init|=
name|HRegionInfo
operator|.
name|convert
argument_list|(
name|HRegionInfo
operator|.
name|FIRST_META_REGIONINFO
argument_list|)
decl_stmt|;
name|HBaseProtos
operator|.
name|RegionInfo
name|normal_ri
init|=
name|HRegionInfo
operator|.
name|convert
argument_list|(
operator|new
name|HRegionInfo
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"test:table"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"a"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"b"
argument_list|)
argument_list|,
literal|false
argument_list|)
argument_list|)
decl_stmt|;
name|RegionServerStatusProtos
operator|.
name|RegionStateTransition
name|metaTransition
init|=
name|RegionServerStatusProtos
operator|.
name|RegionStateTransition
operator|.
name|newBuilder
argument_list|()
operator|.
name|addRegionInfo
argument_list|(
name|meta_ri
argument_list|)
operator|.
name|setTransitionCode
argument_list|(
name|RegionServerStatusProtos
operator|.
name|RegionStateTransition
operator|.
name|TransitionCode
operator|.
name|CLOSED
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|RegionServerStatusProtos
operator|.
name|RegionStateTransition
name|normalTransition
init|=
name|RegionServerStatusProtos
operator|.
name|RegionStateTransition
operator|.
name|newBuilder
argument_list|()
operator|.
name|addRegionInfo
argument_list|(
name|normal_ri
argument_list|)
operator|.
name|setTransitionCode
argument_list|(
name|RegionServerStatusProtos
operator|.
name|RegionStateTransition
operator|.
name|TransitionCode
operator|.
name|CLOSED
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|RegionServerStatusProtos
operator|.
name|ReportRegionStateTransitionRequest
name|metaTransitionRequest
init|=
name|RegionServerStatusProtos
operator|.
name|ReportRegionStateTransitionRequest
operator|.
name|newBuilder
argument_list|()
operator|.
name|setServer
argument_list|(
name|ProtobufUtil
operator|.
name|toServerName
argument_list|(
name|ServerName
operator|.
name|valueOf
argument_list|(
literal|"locahost:60020"
argument_list|,
literal|100
argument_list|)
argument_list|)
argument_list|)
operator|.
name|addTransition
argument_list|(
name|normalTransition
argument_list|)
operator|.
name|addTransition
argument_list|(
name|metaTransition
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|RegionServerStatusProtos
operator|.
name|ReportRegionStateTransitionRequest
name|normalTransitionRequest
init|=
name|RegionServerStatusProtos
operator|.
name|ReportRegionStateTransitionRequest
operator|.
name|newBuilder
argument_list|()
operator|.
name|setServer
argument_list|(
name|ProtobufUtil
operator|.
name|toServerName
argument_list|(
name|ServerName
operator|.
name|valueOf
argument_list|(
literal|"locahost:60020"
argument_list|,
literal|100
argument_list|)
argument_list|)
argument_list|)
operator|.
name|addTransition
argument_list|(
name|normalTransition
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
specifier|final
name|String
name|reportFuncName
init|=
literal|"ReportRegionStateTransition"
decl_stmt|;
name|checkMethod
argument_list|(
name|conf
argument_list|,
name|reportFuncName
argument_list|,
name|HConstants
operator|.
name|META_QOS
argument_list|,
name|qosFunction
argument_list|,
name|metaTransitionRequest
argument_list|)
expr_stmt|;
name|checkMethod
argument_list|(
name|conf
argument_list|,
name|reportFuncName
argument_list|,
name|HConstants
operator|.
name|HIGH_QOS
argument_list|,
name|qosFunction
argument_list|,
name|normalTransitionRequest
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testAnnotations
parameter_list|()
block|{
name|checkMethod
argument_list|(
name|conf
argument_list|,
literal|"GetLastFlushedSequenceId"
argument_list|,
name|HConstants
operator|.
name|ADMIN_QOS
argument_list|,
name|qosFunction
argument_list|)
expr_stmt|;
name|checkMethod
argument_list|(
name|conf
argument_list|,
literal|"CompactRegion"
argument_list|,
name|HConstants
operator|.
name|ADMIN_QOS
argument_list|,
name|qosFunction
argument_list|)
expr_stmt|;
name|checkMethod
argument_list|(
name|conf
argument_list|,
literal|"GetLastFlushedSequenceId"
argument_list|,
name|HConstants
operator|.
name|ADMIN_QOS
argument_list|,
name|qosFunction
argument_list|)
expr_stmt|;
name|checkMethod
argument_list|(
name|conf
argument_list|,
literal|"GetRegionInfo"
argument_list|,
name|HConstants
operator|.
name|ADMIN_QOS
argument_list|,
name|qosFunction
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

