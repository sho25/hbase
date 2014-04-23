begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Copyright 2010 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|hbase
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
name|rest
operator|.
name|ProtobufMessageHandler
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
name|rest
operator|.
name|provider
operator|.
name|JAXBContextResolver
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
name|Base64
import|;
end_import

begin_import
import|import
name|org
operator|.
name|codehaus
operator|.
name|jackson
operator|.
name|jaxrs
operator|.
name|JacksonJaxbJsonProvider
import|;
end_import

begin_import
import|import
name|org
operator|.
name|codehaus
operator|.
name|jackson
operator|.
name|map
operator|.
name|ObjectMapper
import|;
end_import

begin_import
import|import
name|org
operator|.
name|codehaus
operator|.
name|jackson
operator|.
name|node
operator|.
name|ObjectNode
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
name|javax
operator|.
name|ws
operator|.
name|rs
operator|.
name|core
operator|.
name|MediaType
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|xml
operator|.
name|bind
operator|.
name|JAXBContext
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|xml
operator|.
name|bind
operator|.
name|JAXBException
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

begin_class
annotation|@
name|Category
argument_list|(
name|SmallTests
operator|.
name|class
argument_list|)
specifier|public
specifier|abstract
class|class
name|TestModelBase
parameter_list|<
name|T
parameter_list|>
extends|extends
name|TestCase
block|{
specifier|protected
name|String
name|AS_XML
decl_stmt|;
specifier|protected
name|String
name|AS_PB
decl_stmt|;
specifier|protected
name|String
name|AS_JSON
decl_stmt|;
specifier|protected
name|JAXBContext
name|context
decl_stmt|;
specifier|protected
name|Class
argument_list|<
name|?
argument_list|>
name|clazz
decl_stmt|;
specifier|protected
name|ObjectMapper
name|mapper
decl_stmt|;
specifier|protected
name|TestModelBase
parameter_list|(
name|Class
argument_list|<
name|?
argument_list|>
name|clazz
parameter_list|)
throws|throws
name|Exception
block|{
name|super
argument_list|()
expr_stmt|;
name|this
operator|.
name|clazz
operator|=
name|clazz
expr_stmt|;
name|context
operator|=
operator|new
name|JAXBContextResolver
argument_list|()
operator|.
name|getContext
argument_list|(
name|clazz
argument_list|)
expr_stmt|;
name|mapper
operator|=
operator|new
name|JacksonJaxbJsonProvider
argument_list|()
operator|.
name|locateMapper
argument_list|(
name|clazz
argument_list|,
name|MediaType
operator|.
name|APPLICATION_JSON_TYPE
argument_list|)
expr_stmt|;
block|}
specifier|protected
specifier|abstract
name|T
name|buildTestModel
parameter_list|()
function_decl|;
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unused"
argument_list|)
specifier|protected
name|String
name|toXML
parameter_list|(
name|T
name|model
parameter_list|)
throws|throws
name|JAXBException
block|{
name|StringWriter
name|writer
init|=
operator|new
name|StringWriter
argument_list|()
decl_stmt|;
name|context
operator|.
name|createMarshaller
argument_list|()
operator|.
name|marshal
argument_list|(
name|model
argument_list|,
name|writer
argument_list|)
expr_stmt|;
return|return
name|writer
operator|.
name|toString
argument_list|()
return|;
block|}
specifier|protected
name|String
name|toJSON
parameter_list|(
name|T
name|model
parameter_list|)
throws|throws
name|JAXBException
throws|,
name|IOException
block|{
name|StringWriter
name|writer
init|=
operator|new
name|StringWriter
argument_list|()
decl_stmt|;
name|mapper
operator|.
name|writeValue
argument_list|(
name|writer
argument_list|,
name|model
argument_list|)
expr_stmt|;
comment|//  original marshaller, uncomment this and comment mapper to verify backward compatibility
comment|//  ((JSONJAXBContext)context).createJSONMarshaller().marshallToJSON(model, writer);
return|return
name|writer
operator|.
name|toString
argument_list|()
return|;
block|}
specifier|public
name|T
name|fromJSON
parameter_list|(
name|String
name|json
parameter_list|)
throws|throws
name|JAXBException
throws|,
name|IOException
block|{
return|return
operator|(
name|T
operator|)
name|mapper
operator|.
name|readValue
argument_list|(
name|json
argument_list|,
name|clazz
argument_list|)
return|;
block|}
specifier|public
name|T
name|fromXML
parameter_list|(
name|String
name|xml
parameter_list|)
throws|throws
name|JAXBException
block|{
return|return
operator|(
name|T
operator|)
name|context
operator|.
name|createUnmarshaller
argument_list|()
operator|.
name|unmarshal
argument_list|(
operator|new
name|StringReader
argument_list|(
name|xml
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unused"
argument_list|)
specifier|protected
name|byte
index|[]
name|toPB
parameter_list|(
name|ProtobufMessageHandler
name|model
parameter_list|)
block|{
return|return
name|model
operator|.
name|createProtobufOutput
argument_list|()
return|;
block|}
specifier|protected
name|T
name|fromPB
parameter_list|(
name|String
name|pb
parameter_list|)
throws|throws
name|Exception
block|{
return|return
operator|(
name|T
operator|)
name|clazz
operator|.
name|getMethod
argument_list|(
literal|"getObjectFromMessage"
argument_list|,
name|byte
index|[]
operator|.
expr|class
argument_list|)
operator|.
name|invoke
argument_list|(
name|clazz
operator|.
name|newInstance
argument_list|()
argument_list|,
name|Base64
operator|.
name|decode
argument_list|(
name|AS_PB
argument_list|)
argument_list|)
return|;
block|}
specifier|protected
specifier|abstract
name|void
name|checkModel
parameter_list|(
name|T
name|model
parameter_list|)
function_decl|;
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
specifier|public
name|void
name|testToXML
parameter_list|()
throws|throws
name|Exception
block|{
name|assertEquals
argument_list|(
name|AS_XML
argument_list|,
name|toXML
argument_list|(
name|buildTestModel
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|testToJSON
parameter_list|()
throws|throws
name|Exception
block|{
try|try
block|{
name|ObjectNode
name|expObj
init|=
name|mapper
operator|.
name|readValue
argument_list|(
name|AS_JSON
argument_list|,
name|ObjectNode
operator|.
name|class
argument_list|)
decl_stmt|;
name|ObjectNode
name|actObj
init|=
name|mapper
operator|.
name|readValue
argument_list|(
name|toJSON
argument_list|(
name|buildTestModel
argument_list|()
argument_list|)
argument_list|,
name|ObjectNode
operator|.
name|class
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|expObj
argument_list|,
name|actObj
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|assertEquals
argument_list|(
name|AS_JSON
argument_list|,
name|toJSON
argument_list|(
name|buildTestModel
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|testFromJSON
parameter_list|()
throws|throws
name|Exception
block|{
name|checkModel
argument_list|(
name|fromJSON
argument_list|(
name|AS_JSON
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

