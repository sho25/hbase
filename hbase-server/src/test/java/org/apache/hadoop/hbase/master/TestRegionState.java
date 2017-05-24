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
name|shaded
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|ClusterStatusProtos
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
name|TestRegionState
block|{
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
name|Test
specifier|public
name|void
name|testSerializeDeserialize
parameter_list|()
block|{
specifier|final
name|TableName
name|tableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"testtb"
argument_list|)
decl_stmt|;
for|for
control|(
name|RegionState
operator|.
name|State
name|state
range|:
name|RegionState
operator|.
name|State
operator|.
name|values
argument_list|()
control|)
block|{
name|testSerializeDeserialize
argument_list|(
name|tableName
argument_list|,
name|state
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|testSerializeDeserialize
parameter_list|(
specifier|final
name|TableName
name|tableName
parameter_list|,
specifier|final
name|RegionState
operator|.
name|State
name|state
parameter_list|)
block|{
name|RegionState
name|state1
init|=
operator|new
name|RegionState
argument_list|(
operator|new
name|HRegionInfo
argument_list|(
name|tableName
argument_list|)
argument_list|,
name|state
argument_list|)
decl_stmt|;
name|ClusterStatusProtos
operator|.
name|RegionState
name|protobuf1
init|=
name|state1
operator|.
name|convert
argument_list|()
decl_stmt|;
name|RegionState
name|state2
init|=
name|RegionState
operator|.
name|convert
argument_list|(
name|protobuf1
argument_list|)
decl_stmt|;
name|ClusterStatusProtos
operator|.
name|RegionState
name|protobuf2
init|=
name|state1
operator|.
name|convert
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|"RegionState does not match "
operator|+
name|state
argument_list|,
name|state1
argument_list|,
name|state2
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Protobuf does not match "
operator|+
name|state
argument_list|,
name|protobuf1
argument_list|,
name|protobuf2
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

