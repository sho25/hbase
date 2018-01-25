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
name|util
operator|.
name|Arrays
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
name|TestNamespacesModel
extends|extends
name|TestModelBase
argument_list|<
name|NamespacesModel
argument_list|>
block|{
specifier|public
specifier|static
specifier|final
name|String
name|NAMESPACE_NAME_1
init|=
literal|"testNamespace1"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|NAMESPACE_NAME_2
init|=
literal|"testNamespace2"
decl_stmt|;
specifier|public
name|TestNamespacesModel
parameter_list|()
throws|throws
name|Exception
block|{
name|super
argument_list|(
name|NamespacesModel
operator|.
name|class
argument_list|)
expr_stmt|;
name|AS_XML
operator|=
literal|"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
operator|+
literal|"<Namespaces><Namespace>testNamespace1</Namespace>"
operator|+
literal|"<Namespace>testNamespace2</Namespace></Namespaces>"
expr_stmt|;
name|AS_PB
operator|=
literal|"Cg50ZXN0TmFtZXNwYWNlMQoOdGVzdE5hbWVzcGFjZTI="
expr_stmt|;
name|AS_JSON
operator|=
literal|"{\"Namespace\":[\"testNamespace1\",\"testNamespace2\"]}"
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|NamespacesModel
name|buildTestModel
parameter_list|()
block|{
return|return
name|buildTestModel
argument_list|(
name|NAMESPACE_NAME_1
argument_list|,
name|NAMESPACE_NAME_2
argument_list|)
return|;
block|}
specifier|public
name|NamespacesModel
name|buildTestModel
parameter_list|(
name|String
modifier|...
name|namespaces
parameter_list|)
block|{
name|NamespacesModel
name|model
init|=
operator|new
name|NamespacesModel
argument_list|()
decl_stmt|;
name|model
operator|.
name|setNamespaces
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|namespaces
argument_list|)
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
name|NamespacesModel
name|model
parameter_list|)
block|{
name|checkModel
argument_list|(
name|model
argument_list|,
name|NAMESPACE_NAME_1
argument_list|,
name|NAMESPACE_NAME_2
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|checkModel
parameter_list|(
name|NamespacesModel
name|model
parameter_list|,
name|String
modifier|...
name|namespaceName
parameter_list|)
block|{
name|List
argument_list|<
name|String
argument_list|>
name|namespaces
init|=
name|model
operator|.
name|getNamespaces
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|namespaceName
operator|.
name|length
argument_list|,
name|namespaces
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
literal|0
init|;
name|i
operator|<
name|namespaceName
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|assertTrue
argument_list|(
name|namespaces
operator|.
name|contains
argument_list|(
name|namespaceName
index|[
name|i
index|]
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
annotation|@
name|Test
specifier|public
name|void
name|testBuildModel
parameter_list|()
throws|throws
name|Exception
block|{
name|checkModel
argument_list|(
name|buildTestModel
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
annotation|@
name|Test
specifier|public
name|void
name|testFromXML
parameter_list|()
throws|throws
name|Exception
block|{
name|checkModel
argument_list|(
name|fromXML
argument_list|(
name|AS_XML
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
annotation|@
name|Test
specifier|public
name|void
name|testFromPB
parameter_list|()
throws|throws
name|Exception
block|{
name|checkModel
argument_list|(
name|fromPB
argument_list|(
name|AS_PB
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

