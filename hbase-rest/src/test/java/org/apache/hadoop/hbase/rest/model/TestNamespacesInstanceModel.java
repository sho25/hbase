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
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
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
name|TestNamespacesInstanceModel
extends|extends
name|TestModelBase
argument_list|<
name|NamespacesInstanceModel
argument_list|>
block|{
specifier|public
specifier|static
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|NAMESPACE_PROPERTIES
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|()
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|NAMESPACE_NAME
init|=
literal|"namespaceName"
decl_stmt|;
specifier|public
name|TestNamespacesInstanceModel
parameter_list|()
throws|throws
name|Exception
block|{
name|super
argument_list|(
name|NamespacesInstanceModel
operator|.
name|class
argument_list|)
expr_stmt|;
name|NAMESPACE_PROPERTIES
operator|.
name|put
argument_list|(
literal|"KEY_1"
argument_list|,
literal|"VALUE_1"
argument_list|)
expr_stmt|;
name|NAMESPACE_PROPERTIES
operator|.
name|put
argument_list|(
literal|"KEY_2"
argument_list|,
literal|"VALUE_2"
argument_list|)
expr_stmt|;
name|NAMESPACE_PROPERTIES
operator|.
name|put
argument_list|(
literal|"NAME"
argument_list|,
literal|"testNamespace"
argument_list|)
expr_stmt|;
name|AS_XML
operator|=
literal|"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
operator|+
literal|"<NamespaceProperties><properties><entry><key>NAME</key><value>testNamespace"
operator|+
literal|"</value></entry><entry><key>KEY_2</key><value>VALUE_2"
operator|+
literal|"</value></entry><entry><key>KEY_1</key><value>VALUE_1</value></entry>"
operator|+
literal|"</properties></NamespaceProperties>"
expr_stmt|;
name|AS_PB
operator|=
literal|"ChUKBE5BTUUSDXRlc3ROYW1lc3BhY2UKEAoFS0VZXzESB1ZBTFVFXzEKEAoFS0VZXzISB1ZBTFVFXzI="
expr_stmt|;
name|AS_JSON
operator|=
literal|"{\"properties\":{\"NAME\":\"testNamespace\","
operator|+
literal|"\"KEY_1\":\"VALUE_1\",\"KEY_2\":\"VALUE_2\"}}"
expr_stmt|;
block|}
specifier|protected
name|NamespacesInstanceModel
name|buildTestModel
parameter_list|()
block|{
return|return
name|buildTestModel
argument_list|(
name|NAMESPACE_NAME
argument_list|,
name|NAMESPACE_PROPERTIES
argument_list|)
return|;
block|}
specifier|public
name|NamespacesInstanceModel
name|buildTestModel
parameter_list|(
name|String
name|namespace
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|properties
parameter_list|)
block|{
name|NamespacesInstanceModel
name|model
init|=
operator|new
name|NamespacesInstanceModel
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|key
range|:
name|properties
operator|.
name|keySet
argument_list|()
control|)
block|{
name|model
operator|.
name|addProperty
argument_list|(
name|key
argument_list|,
name|properties
operator|.
name|get
argument_list|(
name|key
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|model
return|;
block|}
specifier|protected
name|void
name|checkModel
parameter_list|(
name|NamespacesInstanceModel
name|model
parameter_list|)
block|{
name|checkModel
argument_list|(
name|model
argument_list|,
name|NAMESPACE_NAME
argument_list|,
name|NAMESPACE_PROPERTIES
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|checkModel
parameter_list|(
name|NamespacesInstanceModel
name|model
parameter_list|,
name|String
name|namespace
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|properties
parameter_list|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|modProperties
init|=
name|model
operator|.
name|getProperties
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|properties
operator|.
name|size
argument_list|()
argument_list|,
name|modProperties
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
comment|// Namespace name comes from REST URI, not properties.
name|assertNotSame
argument_list|(
name|namespace
argument_list|,
name|model
operator|.
name|getNamespaceName
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|property
range|:
name|properties
operator|.
name|keySet
argument_list|()
control|)
block|{
name|assertEquals
argument_list|(
name|properties
operator|.
name|get
argument_list|(
name|property
argument_list|)
argument_list|,
name|modProperties
operator|.
name|get
argument_list|(
name|property
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
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

