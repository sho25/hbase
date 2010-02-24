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
name|stargate
operator|.
name|provider
operator|.
name|producer
package|;
end_package

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
name|OutputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|annotation
operator|.
name|Annotation
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|Type
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
name|WeakHashMap
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
name|Produces
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
name|WebApplicationException
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
name|ws
operator|.
name|rs
operator|.
name|core
operator|.
name|MultivaluedMap
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
name|ext
operator|.
name|MessageBodyWriter
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
name|ext
operator|.
name|Provider
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
name|stargate
operator|.
name|Constants
import|;
end_import

begin_comment
comment|/**  * An adapter between Jersey and Object.toString(). Hooks up plain text output  * to the Jersey content handling framework.   * Jersey will first call getSize() to learn the number of bytes that will be  * sent, then writeTo to perform the actual I/O.  */
end_comment

begin_class
annotation|@
name|Provider
annotation|@
name|Produces
argument_list|(
name|Constants
operator|.
name|MIMETYPE_TEXT
argument_list|)
specifier|public
class|class
name|PlainTextMessageBodyProducer
implements|implements
name|MessageBodyWriter
argument_list|<
name|Object
argument_list|>
block|{
specifier|private
name|Map
argument_list|<
name|Object
argument_list|,
name|byte
index|[]
argument_list|>
name|buffer
init|=
operator|new
name|WeakHashMap
argument_list|<
name|Object
argument_list|,
name|byte
index|[]
argument_list|>
argument_list|()
decl_stmt|;
annotation|@
name|Override
specifier|public
name|boolean
name|isWriteable
parameter_list|(
name|Class
argument_list|<
name|?
argument_list|>
name|arg0
parameter_list|,
name|Type
name|arg1
parameter_list|,
name|Annotation
index|[]
name|arg2
parameter_list|,
name|MediaType
name|arg3
parameter_list|)
block|{
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getSize
parameter_list|(
name|Object
name|object
parameter_list|,
name|Class
argument_list|<
name|?
argument_list|>
name|type
parameter_list|,
name|Type
name|genericType
parameter_list|,
name|Annotation
index|[]
name|annotations
parameter_list|,
name|MediaType
name|mediaType
parameter_list|)
block|{
name|byte
index|[]
name|bytes
init|=
name|object
operator|.
name|toString
argument_list|()
operator|.
name|getBytes
argument_list|()
decl_stmt|;
name|buffer
operator|.
name|put
argument_list|(
name|object
argument_list|,
name|bytes
argument_list|)
expr_stmt|;
return|return
name|bytes
operator|.
name|length
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|writeTo
parameter_list|(
name|Object
name|object
parameter_list|,
name|Class
argument_list|<
name|?
argument_list|>
name|type
parameter_list|,
name|Type
name|genericType
parameter_list|,
name|Annotation
index|[]
name|annotations
parameter_list|,
name|MediaType
name|mediaType
parameter_list|,
name|MultivaluedMap
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|httpHeaders
parameter_list|,
name|OutputStream
name|outStream
parameter_list|)
throws|throws
name|IOException
throws|,
name|WebApplicationException
block|{
name|outStream
operator|.
name|write
argument_list|(
name|buffer
operator|.
name|remove
argument_list|(
name|object
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

