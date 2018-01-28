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
name|http
operator|.
name|conf
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|StringReader
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|StringWriter
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashSet
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
name|java
operator|.
name|util
operator|.
name|Set
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|xml
operator|.
name|parsers
operator|.
name|DocumentBuilder
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|xml
operator|.
name|parsers
operator|.
name|DocumentBuilderFactory
import|;
end_import

begin_import
import|import
name|junit
operator|.
name|framework
operator|.
name|TestCase
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
name|testclassification
operator|.
name|MiscTests
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
name|eclipse
operator|.
name|jetty
operator|.
name|util
operator|.
name|ajax
operator|.
name|JSON
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
name|w3c
operator|.
name|dom
operator|.
name|Document
import|;
end_import

begin_import
import|import
name|org
operator|.
name|w3c
operator|.
name|dom
operator|.
name|Element
import|;
end_import

begin_import
import|import
name|org
operator|.
name|w3c
operator|.
name|dom
operator|.
name|Node
import|;
end_import

begin_import
import|import
name|org
operator|.
name|w3c
operator|.
name|dom
operator|.
name|NodeList
import|;
end_import

begin_import
import|import
name|org
operator|.
name|xml
operator|.
name|sax
operator|.
name|InputSource
import|;
end_import

begin_comment
comment|/**  * Basic test case that the ConfServlet can write configuration  * to its output in XML and JSON format.  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|MiscTests
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
name|TestConfServlet
extends|extends
name|TestCase
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
name|TestConfServlet
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|TEST_KEY
init|=
literal|"testconfservlet.key"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|TEST_VAL
init|=
literal|"testval"
decl_stmt|;
specifier|private
name|Configuration
name|getTestConf
parameter_list|()
block|{
name|Configuration
name|testConf
init|=
operator|new
name|Configuration
argument_list|()
decl_stmt|;
name|testConf
operator|.
name|set
argument_list|(
name|TEST_KEY
argument_list|,
name|TEST_VAL
argument_list|)
expr_stmt|;
return|return
name|testConf
return|;
block|}
annotation|@
name|Test
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|public
name|void
name|testWriteJson
parameter_list|()
throws|throws
name|Exception
block|{
name|StringWriter
name|sw
init|=
operator|new
name|StringWriter
argument_list|()
decl_stmt|;
name|ConfServlet
operator|.
name|writeResponse
argument_list|(
name|getTestConf
argument_list|()
argument_list|,
name|sw
argument_list|,
literal|"json"
argument_list|)
expr_stmt|;
name|String
name|json
init|=
name|sw
operator|.
name|toString
argument_list|()
decl_stmt|;
name|boolean
name|foundSetting
init|=
literal|false
decl_stmt|;
name|Set
argument_list|<
name|String
argument_list|>
name|programSet
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
name|programSet
operator|.
name|add
argument_list|(
literal|"programatically"
argument_list|)
expr_stmt|;
name|programSet
operator|.
name|add
argument_list|(
literal|"programmatically"
argument_list|)
expr_stmt|;
name|Object
name|parsed
init|=
name|JSON
operator|.
name|parse
argument_list|(
name|json
argument_list|)
decl_stmt|;
name|Object
index|[]
name|properties
init|=
operator|(
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
index|[]
argument_list|>
operator|)
name|parsed
operator|)
operator|.
name|get
argument_list|(
literal|"properties"
argument_list|)
decl_stmt|;
for|for
control|(
name|Object
name|o
range|:
name|properties
control|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|propertyInfo
init|=
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
operator|)
name|o
decl_stmt|;
name|String
name|key
init|=
operator|(
name|String
operator|)
name|propertyInfo
operator|.
name|get
argument_list|(
literal|"key"
argument_list|)
decl_stmt|;
name|String
name|val
init|=
operator|(
name|String
operator|)
name|propertyInfo
operator|.
name|get
argument_list|(
literal|"value"
argument_list|)
decl_stmt|;
name|String
name|resource
init|=
operator|(
name|String
operator|)
name|propertyInfo
operator|.
name|get
argument_list|(
literal|"resource"
argument_list|)
decl_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"k: "
operator|+
name|key
operator|+
literal|" v: "
operator|+
name|val
operator|+
literal|" r: "
operator|+
name|resource
argument_list|)
expr_stmt|;
if|if
condition|(
name|TEST_KEY
operator|.
name|equals
argument_list|(
name|key
argument_list|)
operator|&&
name|TEST_VAL
operator|.
name|equals
argument_list|(
name|val
argument_list|)
operator|&&
name|programSet
operator|.
name|contains
argument_list|(
name|resource
argument_list|)
condition|)
block|{
name|foundSetting
operator|=
literal|true
expr_stmt|;
block|}
block|}
name|assertTrue
argument_list|(
name|foundSetting
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testWriteXml
parameter_list|()
throws|throws
name|Exception
block|{
name|StringWriter
name|sw
init|=
operator|new
name|StringWriter
argument_list|()
decl_stmt|;
name|ConfServlet
operator|.
name|writeResponse
argument_list|(
name|getTestConf
argument_list|()
argument_list|,
name|sw
argument_list|,
literal|"xml"
argument_list|)
expr_stmt|;
name|String
name|xml
init|=
name|sw
operator|.
name|toString
argument_list|()
decl_stmt|;
name|DocumentBuilderFactory
name|docBuilderFactory
init|=
name|DocumentBuilderFactory
operator|.
name|newInstance
argument_list|()
decl_stmt|;
name|DocumentBuilder
name|builder
init|=
name|docBuilderFactory
operator|.
name|newDocumentBuilder
argument_list|()
decl_stmt|;
name|Document
name|doc
init|=
name|builder
operator|.
name|parse
argument_list|(
operator|new
name|InputSource
argument_list|(
operator|new
name|StringReader
argument_list|(
name|xml
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|NodeList
name|nameNodes
init|=
name|doc
operator|.
name|getElementsByTagName
argument_list|(
literal|"name"
argument_list|)
decl_stmt|;
name|boolean
name|foundSetting
init|=
literal|false
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|nameNodes
operator|.
name|getLength
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|Node
name|nameNode
init|=
name|nameNodes
operator|.
name|item
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|String
name|key
init|=
name|nameNode
operator|.
name|getTextContent
argument_list|()
decl_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"xml key: "
operator|+
name|key
argument_list|)
expr_stmt|;
if|if
condition|(
name|TEST_KEY
operator|.
name|equals
argument_list|(
name|key
argument_list|)
condition|)
block|{
name|foundSetting
operator|=
literal|true
expr_stmt|;
name|Element
name|propertyElem
init|=
operator|(
name|Element
operator|)
name|nameNode
operator|.
name|getParentNode
argument_list|()
decl_stmt|;
name|String
name|val
init|=
name|propertyElem
operator|.
name|getElementsByTagName
argument_list|(
literal|"value"
argument_list|)
operator|.
name|item
argument_list|(
literal|0
argument_list|)
operator|.
name|getTextContent
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|TEST_VAL
argument_list|,
name|val
argument_list|)
expr_stmt|;
block|}
block|}
name|assertTrue
argument_list|(
name|foundSetting
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testBadFormat
parameter_list|()
throws|throws
name|Exception
block|{
name|StringWriter
name|sw
init|=
operator|new
name|StringWriter
argument_list|()
decl_stmt|;
try|try
block|{
name|ConfServlet
operator|.
name|writeResponse
argument_list|(
name|getTestConf
argument_list|()
argument_list|,
name|sw
argument_list|,
literal|"not a format"
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"writeResponse with bad format didn't throw!"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ConfServlet
operator|.
name|BadFormatException
name|bfe
parameter_list|)
block|{
comment|// expected
block|}
name|assertEquals
argument_list|(
literal|""
argument_list|,
name|sw
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

