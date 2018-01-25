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
name|rest
operator|.
name|model
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
name|RestTests
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
name|experimental
operator|.
name|categories
operator|.
name|Category
import|;
end_import

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|RestTests
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
name|TestStorageClusterVersionModel
extends|extends
name|TestModelBase
argument_list|<
name|StorageClusterVersionModel
argument_list|>
block|{
specifier|private
specifier|static
specifier|final
name|String
name|VERSION
init|=
literal|"0.0.1-testing"
decl_stmt|;
specifier|public
name|TestStorageClusterVersionModel
parameter_list|()
throws|throws
name|Exception
block|{
name|super
argument_list|(
name|StorageClusterVersionModel
operator|.
name|class
argument_list|)
expr_stmt|;
name|AS_XML
operator|=
literal|"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
operator|+
literal|"<ClusterVersion Version=\""
operator|+
name|VERSION
operator|+
literal|"\"/>"
expr_stmt|;
name|AS_JSON
operator|=
literal|"{\"Version\": \"0.0.1-testing\"}"
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|StorageClusterVersionModel
name|buildTestModel
parameter_list|()
block|{
name|StorageClusterVersionModel
name|model
init|=
operator|new
name|StorageClusterVersionModel
argument_list|()
decl_stmt|;
name|model
operator|.
name|setVersion
argument_list|(
name|VERSION
argument_list|)
expr_stmt|;
return|return
name|model
return|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|checkModel
parameter_list|(
name|StorageClusterVersionModel
name|model
parameter_list|)
block|{
name|assertEquals
argument_list|(
name|VERSION
argument_list|,
name|model
operator|.
name|getVersion
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|testFromPB
parameter_list|()
throws|throws
name|Exception
block|{
comment|//ignore test no pb
block|}
block|}
end_class

end_unit

