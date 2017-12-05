begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|coprocessor
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataInput
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataOutput
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|File
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
name|InputStream
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
name|io
operator|.
name|Reader
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|Writer
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|InetSocketAddress
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|URL
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collection
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Iterator
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
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|TimeUnit
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|regex
operator|.
name|Pattern
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
name|fs
operator|.
name|Path
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|yetus
operator|.
name|audience
operator|.
name|InterfaceAudience
import|;
end_import

begin_comment
comment|/**  * Wraps a Configuration to make it read-only.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
class|class
name|ReadOnlyConfiguration
extends|extends
name|Configuration
block|{
specifier|private
specifier|final
name|Configuration
name|conf
decl_stmt|;
name|ReadOnlyConfiguration
parameter_list|(
specifier|final
name|Configuration
name|conf
parameter_list|)
block|{
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setDeprecatedProperties
parameter_list|()
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Read-only Configuration"
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|addResource
parameter_list|(
name|String
name|name
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Read-only Configuration"
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|addResource
parameter_list|(
name|URL
name|url
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Read-only Configuration"
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|addResource
parameter_list|(
name|Path
name|file
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Read-only Configuration"
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|addResource
parameter_list|(
name|InputStream
name|in
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Read-only Configuration"
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|addResource
parameter_list|(
name|InputStream
name|in
parameter_list|,
name|String
name|name
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Read-only Configuration"
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|addResource
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Read-only Configuration"
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|reloadConfiguration
parameter_list|()
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Read-only Configuration"
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|get
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
name|conf
operator|.
name|get
argument_list|(
name|name
argument_list|)
return|;
block|}
specifier|public
name|void
name|setAllowNullValueProperties
parameter_list|(
name|boolean
name|val
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Read-only Configuration"
argument_list|)
throw|;
block|}
specifier|public
name|String
name|getTrimmed
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
name|conf
operator|.
name|getTrimmed
argument_list|(
name|name
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getTrimmed
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|defaultValue
parameter_list|)
block|{
return|return
name|conf
operator|.
name|getTrimmed
argument_list|(
name|name
argument_list|,
name|defaultValue
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getRaw
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
name|conf
operator|.
name|getRaw
argument_list|(
name|name
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|set
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|value
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Read-only Configuration"
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|set
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|value
parameter_list|,
name|String
name|source
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Read-only Configuration"
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|unset
parameter_list|(
name|String
name|name
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Read-only Configuration"
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setIfUnset
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|value
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Read-only Configuration"
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|get
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|defaultValue
parameter_list|)
block|{
return|return
name|conf
operator|.
name|get
argument_list|(
name|name
argument_list|,
name|defaultValue
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getInt
parameter_list|(
name|String
name|name
parameter_list|,
name|int
name|defaultValue
parameter_list|)
block|{
return|return
name|conf
operator|.
name|getInt
argument_list|(
name|name
argument_list|,
name|defaultValue
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
index|[]
name|getInts
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
name|conf
operator|.
name|getInts
argument_list|(
name|name
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setInt
parameter_list|(
name|String
name|name
parameter_list|,
name|int
name|value
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Read-only Configuration"
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getLong
parameter_list|(
name|String
name|name
parameter_list|,
name|long
name|defaultValue
parameter_list|)
block|{
return|return
name|conf
operator|.
name|getLong
argument_list|(
name|name
argument_list|,
name|defaultValue
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getLongBytes
parameter_list|(
name|String
name|name
parameter_list|,
name|long
name|defaultValue
parameter_list|)
block|{
return|return
name|conf
operator|.
name|getLongBytes
argument_list|(
name|name
argument_list|,
name|defaultValue
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setLong
parameter_list|(
name|String
name|name
parameter_list|,
name|long
name|value
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Read-only Configuration"
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|float
name|getFloat
parameter_list|(
name|String
name|name
parameter_list|,
name|float
name|defaultValue
parameter_list|)
block|{
return|return
name|conf
operator|.
name|getFloat
argument_list|(
name|name
argument_list|,
name|defaultValue
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setFloat
parameter_list|(
name|String
name|name
parameter_list|,
name|float
name|value
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Read-only Configuration"
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|double
name|getDouble
parameter_list|(
name|String
name|name
parameter_list|,
name|double
name|defaultValue
parameter_list|)
block|{
return|return
name|conf
operator|.
name|getDouble
argument_list|(
name|name
argument_list|,
name|defaultValue
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setDouble
parameter_list|(
name|String
name|name
parameter_list|,
name|double
name|value
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Read-only Configuration"
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|getBoolean
parameter_list|(
name|String
name|name
parameter_list|,
name|boolean
name|defaultValue
parameter_list|)
block|{
return|return
name|conf
operator|.
name|getBoolean
argument_list|(
name|name
argument_list|,
name|defaultValue
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setBoolean
parameter_list|(
name|String
name|name
parameter_list|,
name|boolean
name|value
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Read-only Configuration"
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setBooleanIfUnset
parameter_list|(
name|String
name|name
parameter_list|,
name|boolean
name|value
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Read-only Configuration"
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
parameter_list|<
name|T
extends|extends
name|Enum
argument_list|<
name|T
argument_list|>
parameter_list|>
name|void
name|setEnum
parameter_list|(
name|String
name|name
parameter_list|,
name|T
name|value
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Read-only Configuration"
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
parameter_list|<
name|T
extends|extends
name|Enum
argument_list|<
name|T
argument_list|>
parameter_list|>
name|T
name|getEnum
parameter_list|(
name|String
name|name
parameter_list|,
name|T
name|defaultValue
parameter_list|)
block|{
return|return
name|conf
operator|.
name|getEnum
argument_list|(
name|name
argument_list|,
name|defaultValue
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setTimeDuration
parameter_list|(
name|String
name|name
parameter_list|,
name|long
name|value
parameter_list|,
name|TimeUnit
name|unit
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Read-only Configuration"
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getTimeDuration
parameter_list|(
name|String
name|name
parameter_list|,
name|long
name|defaultValue
parameter_list|,
name|TimeUnit
name|unit
parameter_list|)
block|{
return|return
name|conf
operator|.
name|getTimeDuration
argument_list|(
name|name
argument_list|,
name|defaultValue
argument_list|,
name|unit
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Pattern
name|getPattern
parameter_list|(
name|String
name|name
parameter_list|,
name|Pattern
name|defaultValue
parameter_list|)
block|{
return|return
name|conf
operator|.
name|getPattern
argument_list|(
name|name
argument_list|,
name|defaultValue
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setPattern
parameter_list|(
name|String
name|name
parameter_list|,
name|Pattern
name|pattern
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Read-only Configuration"
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|String
index|[]
name|getPropertySources
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
name|conf
operator|.
name|getPropertySources
argument_list|(
name|name
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Configuration
operator|.
name|IntegerRanges
name|getRange
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|defaultValue
parameter_list|)
block|{
return|return
name|conf
operator|.
name|getRange
argument_list|(
name|name
argument_list|,
name|defaultValue
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Collection
argument_list|<
name|String
argument_list|>
name|getStringCollection
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
name|conf
operator|.
name|getStringCollection
argument_list|(
name|name
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
index|[]
name|getStrings
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
name|conf
operator|.
name|getStrings
argument_list|(
name|name
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
index|[]
name|getStrings
parameter_list|(
name|String
name|name
parameter_list|,
name|String
modifier|...
name|defaultValue
parameter_list|)
block|{
return|return
name|conf
operator|.
name|getStrings
argument_list|(
name|name
argument_list|,
name|defaultValue
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Collection
argument_list|<
name|String
argument_list|>
name|getTrimmedStringCollection
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
name|conf
operator|.
name|getTrimmedStringCollection
argument_list|(
name|name
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
index|[]
name|getTrimmedStrings
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
name|conf
operator|.
name|getTrimmedStrings
argument_list|(
name|name
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
index|[]
name|getTrimmedStrings
parameter_list|(
name|String
name|name
parameter_list|,
name|String
modifier|...
name|defaultValue
parameter_list|)
block|{
return|return
name|conf
operator|.
name|getTrimmedStrings
argument_list|(
name|name
argument_list|,
name|defaultValue
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setStrings
parameter_list|(
name|String
name|name
parameter_list|,
name|String
modifier|...
name|values
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Read-only Configuration"
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|char
index|[]
name|getPassword
parameter_list|(
name|String
name|name
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|conf
operator|.
name|getPassword
argument_list|(
name|name
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|InetSocketAddress
name|getSocketAddr
parameter_list|(
name|String
name|hostProperty
parameter_list|,
name|String
name|addressProperty
parameter_list|,
name|String
name|defaultAddressValue
parameter_list|,
name|int
name|defaultPort
parameter_list|)
block|{
return|return
name|conf
operator|.
name|getSocketAddr
argument_list|(
name|hostProperty
argument_list|,
name|addressProperty
argument_list|,
name|defaultAddressValue
argument_list|,
name|defaultPort
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|InetSocketAddress
name|getSocketAddr
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|defaultAddress
parameter_list|,
name|int
name|defaultPort
parameter_list|)
block|{
return|return
name|conf
operator|.
name|getSocketAddr
argument_list|(
name|name
argument_list|,
name|defaultAddress
argument_list|,
name|defaultPort
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setSocketAddr
parameter_list|(
name|String
name|name
parameter_list|,
name|InetSocketAddress
name|addr
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Read-only Configuration"
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|InetSocketAddress
name|updateConnectAddr
parameter_list|(
name|String
name|hostProperty
parameter_list|,
name|String
name|addressProperty
parameter_list|,
name|String
name|defaultAddressValue
parameter_list|,
name|InetSocketAddress
name|addr
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Read-only Configuration"
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|InetSocketAddress
name|updateConnectAddr
parameter_list|(
name|String
name|name
parameter_list|,
name|InetSocketAddress
name|addr
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Read-only Configuration"
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|Class
argument_list|<
name|?
argument_list|>
name|getClassByName
parameter_list|(
name|String
name|name
parameter_list|)
throws|throws
name|ClassNotFoundException
block|{
return|return
name|conf
operator|.
name|getClassByName
argument_list|(
name|name
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Class
argument_list|<
name|?
argument_list|>
name|getClassByNameOrNull
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
name|conf
operator|.
name|getClassByNameOrNull
argument_list|(
name|name
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Class
argument_list|<
name|?
argument_list|>
index|[]
name|getClasses
parameter_list|(
name|String
name|name
parameter_list|,
name|Class
argument_list|<
name|?
argument_list|>
index|[]
name|defaultValue
parameter_list|)
block|{
return|return
name|conf
operator|.
name|getClasses
argument_list|(
name|name
argument_list|,
name|defaultValue
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Class
argument_list|<
name|?
argument_list|>
name|getClass
parameter_list|(
name|String
name|name
parameter_list|,
name|Class
argument_list|<
name|?
argument_list|>
name|defaultValue
parameter_list|)
block|{
return|return
name|conf
operator|.
name|getClass
argument_list|(
name|name
argument_list|,
name|defaultValue
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
parameter_list|<
name|U
parameter_list|>
name|Class
argument_list|<
name|?
extends|extends
name|U
argument_list|>
name|getClass
parameter_list|(
name|String
name|name
parameter_list|,
name|Class
argument_list|<
name|?
extends|extends
name|U
argument_list|>
name|defaultValue
parameter_list|,
name|Class
argument_list|<
name|U
argument_list|>
name|xface
parameter_list|)
block|{
return|return
name|conf
operator|.
name|getClass
argument_list|(
name|name
argument_list|,
name|defaultValue
argument_list|,
name|xface
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
parameter_list|<
name|U
parameter_list|>
name|List
argument_list|<
name|U
argument_list|>
name|getInstances
parameter_list|(
name|String
name|name
parameter_list|,
name|Class
argument_list|<
name|U
argument_list|>
name|xface
parameter_list|)
block|{
return|return
name|conf
operator|.
name|getInstances
argument_list|(
name|name
argument_list|,
name|xface
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setClass
parameter_list|(
name|String
name|name
parameter_list|,
name|Class
argument_list|<
name|?
argument_list|>
name|theClass
parameter_list|,
name|Class
argument_list|<
name|?
argument_list|>
name|xface
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Read-only Configuration"
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|Path
name|getLocalPath
parameter_list|(
name|String
name|dirsProp
parameter_list|,
name|String
name|path
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|conf
operator|.
name|getLocalPath
argument_list|(
name|dirsProp
argument_list|,
name|path
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|File
name|getFile
parameter_list|(
name|String
name|dirsProp
parameter_list|,
name|String
name|path
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|conf
operator|.
name|getFile
argument_list|(
name|dirsProp
argument_list|,
name|path
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|URL
name|getResource
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
name|conf
operator|.
name|getResource
argument_list|(
name|name
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|InputStream
name|getConfResourceAsInputStream
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
name|conf
operator|.
name|getConfResourceAsInputStream
argument_list|(
name|name
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Reader
name|getConfResourceAsReader
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
name|conf
operator|.
name|getConfResourceAsReader
argument_list|(
name|name
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Set
argument_list|<
name|String
argument_list|>
name|getFinalParameters
parameter_list|()
block|{
return|return
name|conf
operator|.
name|getFinalParameters
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|size
parameter_list|()
block|{
return|return
name|conf
operator|.
name|size
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|clear
parameter_list|()
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Read-only Configuration"
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|Iterator
argument_list|<
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
argument_list|>
name|iterator
parameter_list|()
block|{
return|return
name|conf
operator|.
name|iterator
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|writeXml
parameter_list|(
name|OutputStream
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|conf
operator|.
name|writeXml
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|writeXml
parameter_list|(
name|Writer
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|conf
operator|.
name|writeXml
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|ClassLoader
name|getClassLoader
parameter_list|()
block|{
return|return
name|conf
operator|.
name|getClassLoader
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setClassLoader
parameter_list|(
name|ClassLoader
name|classLoader
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Read-only Configuration"
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|conf
operator|.
name|toString
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setQuietMode
parameter_list|(
name|boolean
name|quietmode
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Read-only Configuration"
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|readFields
parameter_list|(
name|DataInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Read-only Configuration"
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|write
parameter_list|(
name|DataOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|conf
operator|.
name|write
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|getValByRegex
parameter_list|(
name|String
name|regex
parameter_list|)
block|{
return|return
name|conf
operator|.
name|getValByRegex
argument_list|(
name|regex
argument_list|)
return|;
block|}
block|}
end_class

end_unit

