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
name|regionserver
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
name|generated
operator|.
name|ClientProtos
operator|.
name|MultiRequest
import|;
end_import

begin_comment
comment|/**  * Basic test that qos function is sort of working; i.e. a change in method naming style  * over in pb doesn't break it.  */
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
name|TestQosFunction
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
name|TestQosFunction
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
name|RSRpcServices
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
name|AnnotationReadingPriorityFunction
argument_list|(
name|rpcServices
argument_list|,
name|RSRpcServices
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testPriority
parameter_list|()
block|{
comment|// Set method name in pb style with the method name capitalized.
name|checkMethod
argument_list|(
name|conf
argument_list|,
literal|"ReplicateWALEntry"
argument_list|,
name|HConstants
operator|.
name|REPLICATION_QOS
argument_list|,
name|qosFunction
argument_list|)
expr_stmt|;
comment|// Set method name in pb style with the method name capitalized.
name|checkMethod
argument_list|(
name|conf
argument_list|,
literal|"OpenRegion"
argument_list|,
name|HConstants
operator|.
name|ADMIN_QOS
argument_list|,
name|qosFunction
argument_list|)
expr_stmt|;
comment|// Check multi works.
name|checkMethod
argument_list|(
name|conf
argument_list|,
literal|"Multi"
argument_list|,
name|HConstants
operator|.
name|NORMAL_QOS
argument_list|,
name|qosFunction
argument_list|,
name|MultiRequest
operator|.
name|getDefaultInstance
argument_list|()
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
literal|"CloseRegion"
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
literal|"FlushRegion"
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

