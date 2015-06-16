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
name|client
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|ByteArrayInputStream
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
name|InterruptedIOException
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
name|javax
operator|.
name|xml
operator|.
name|bind
operator|.
name|Unmarshaller
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
name|classification
operator|.
name|InterfaceAudience
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
name|classification
operator|.
name|InterfaceStability
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
name|HTableDescriptor
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
name|Constants
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
name|model
operator|.
name|StorageClusterStatusModel
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
name|model
operator|.
name|StorageClusterVersionModel
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
name|model
operator|.
name|TableListModel
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
name|model
operator|.
name|TableSchemaModel
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
name|model
operator|.
name|VersionModel
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

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
annotation|@
name|InterfaceStability
operator|.
name|Stable
specifier|public
class|class
name|RemoteAdmin
block|{
specifier|final
name|Client
name|client
decl_stmt|;
specifier|final
name|Configuration
name|conf
decl_stmt|;
specifier|final
name|String
name|accessToken
decl_stmt|;
specifier|final
name|int
name|maxRetries
decl_stmt|;
specifier|final
name|long
name|sleepTime
decl_stmt|;
comment|// This unmarshaller is necessary for getting the /version/cluster resource.
comment|// This resource does not support protobufs. Therefore this is necessary to
comment|// request/interpret it as XML.
specifier|private
specifier|static
specifier|volatile
name|Unmarshaller
name|versionClusterUnmarshaller
decl_stmt|;
comment|/**    * Constructor    *     * @param client    * @param conf    */
specifier|public
name|RemoteAdmin
parameter_list|(
name|Client
name|client
parameter_list|,
name|Configuration
name|conf
parameter_list|)
block|{
name|this
argument_list|(
name|client
argument_list|,
name|conf
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
specifier|static
name|Unmarshaller
name|getUnmarsheller
parameter_list|()
throws|throws
name|JAXBException
block|{
if|if
condition|(
name|versionClusterUnmarshaller
operator|==
literal|null
condition|)
block|{
name|RemoteAdmin
operator|.
name|versionClusterUnmarshaller
operator|=
name|JAXBContext
operator|.
name|newInstance
argument_list|(
name|StorageClusterVersionModel
operator|.
name|class
argument_list|)
operator|.
name|createUnmarshaller
argument_list|()
expr_stmt|;
block|}
return|return
name|RemoteAdmin
operator|.
name|versionClusterUnmarshaller
return|;
block|}
comment|/**    * Constructor    * @param client    * @param conf    * @param accessToken    */
specifier|public
name|RemoteAdmin
parameter_list|(
name|Client
name|client
parameter_list|,
name|Configuration
name|conf
parameter_list|,
name|String
name|accessToken
parameter_list|)
block|{
name|this
operator|.
name|client
operator|=
name|client
expr_stmt|;
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|this
operator|.
name|accessToken
operator|=
name|accessToken
expr_stmt|;
name|this
operator|.
name|maxRetries
operator|=
name|conf
operator|.
name|getInt
argument_list|(
literal|"hbase.rest.client.max.retries"
argument_list|,
literal|10
argument_list|)
expr_stmt|;
name|this
operator|.
name|sleepTime
operator|=
name|conf
operator|.
name|getLong
argument_list|(
literal|"hbase.rest.client.sleep"
argument_list|,
literal|1000
argument_list|)
expr_stmt|;
block|}
comment|/**    * @param tableName name of table to check    * @return true if all regions of the table are available    * @throws IOException if a remote or network exception occurs    */
specifier|public
name|boolean
name|isTableAvailable
parameter_list|(
name|String
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|isTableAvailable
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|tableName
argument_list|)
argument_list|)
return|;
block|}
comment|/**    * @return string representing the rest api's version    * @throws IOEXception    *           if the endpoint does not exist, there is a timeout, or some other    *           general failure mode    */
specifier|public
name|VersionModel
name|getRestVersion
parameter_list|()
throws|throws
name|IOException
block|{
name|StringBuilder
name|path
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|path
operator|.
name|append
argument_list|(
literal|'/'
argument_list|)
expr_stmt|;
if|if
condition|(
name|accessToken
operator|!=
literal|null
condition|)
block|{
name|path
operator|.
name|append
argument_list|(
name|accessToken
argument_list|)
expr_stmt|;
name|path
operator|.
name|append
argument_list|(
literal|'/'
argument_list|)
expr_stmt|;
block|}
name|path
operator|.
name|append
argument_list|(
literal|"version/rest"
argument_list|)
expr_stmt|;
name|int
name|code
init|=
literal|0
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
name|maxRetries
condition|;
name|i
operator|++
control|)
block|{
name|Response
name|response
init|=
name|client
operator|.
name|get
argument_list|(
name|path
operator|.
name|toString
argument_list|()
argument_list|,
name|Constants
operator|.
name|MIMETYPE_PROTOBUF
argument_list|)
decl_stmt|;
name|code
operator|=
name|response
operator|.
name|getCode
argument_list|()
expr_stmt|;
switch|switch
condition|(
name|code
condition|)
block|{
case|case
literal|200
case|:
name|VersionModel
name|v
init|=
operator|new
name|VersionModel
argument_list|()
decl_stmt|;
return|return
operator|(
name|VersionModel
operator|)
name|v
operator|.
name|getObjectFromMessage
argument_list|(
name|response
operator|.
name|getBody
argument_list|()
argument_list|)
return|;
case|case
literal|404
case|:
throw|throw
operator|new
name|IOException
argument_list|(
literal|"REST version not found"
argument_list|)
throw|;
case|case
literal|509
case|:
try|try
block|{
name|Thread
operator|.
name|sleep
argument_list|(
name|sleepTime
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
throw|throw
operator|(
name|InterruptedIOException
operator|)
operator|new
name|InterruptedIOException
argument_list|()
operator|.
name|initCause
argument_list|(
name|e
argument_list|)
throw|;
block|}
break|break;
default|default:
throw|throw
operator|new
name|IOException
argument_list|(
literal|"get request to "
operator|+
name|path
operator|.
name|toString
argument_list|()
operator|+
literal|" returned "
operator|+
name|code
argument_list|)
throw|;
block|}
block|}
throw|throw
operator|new
name|IOException
argument_list|(
literal|"get request to "
operator|+
name|path
operator|.
name|toString
argument_list|()
operator|+
literal|" timed out"
argument_list|)
throw|;
block|}
comment|/**    * @return string representing the cluster's version    * @throws IOEXception if the endpoint does not exist, there is a timeout, or some other general failure mode    */
specifier|public
name|StorageClusterStatusModel
name|getClusterStatus
parameter_list|()
throws|throws
name|IOException
block|{
name|StringBuilder
name|path
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|path
operator|.
name|append
argument_list|(
literal|'/'
argument_list|)
expr_stmt|;
if|if
condition|(
name|accessToken
operator|!=
literal|null
condition|)
block|{
name|path
operator|.
name|append
argument_list|(
name|accessToken
argument_list|)
expr_stmt|;
name|path
operator|.
name|append
argument_list|(
literal|'/'
argument_list|)
expr_stmt|;
block|}
name|path
operator|.
name|append
argument_list|(
literal|"status/cluster"
argument_list|)
expr_stmt|;
name|int
name|code
init|=
literal|0
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
name|maxRetries
condition|;
name|i
operator|++
control|)
block|{
name|Response
name|response
init|=
name|client
operator|.
name|get
argument_list|(
name|path
operator|.
name|toString
argument_list|()
argument_list|,
name|Constants
operator|.
name|MIMETYPE_PROTOBUF
argument_list|)
decl_stmt|;
name|code
operator|=
name|response
operator|.
name|getCode
argument_list|()
expr_stmt|;
switch|switch
condition|(
name|code
condition|)
block|{
case|case
literal|200
case|:
name|StorageClusterStatusModel
name|s
init|=
operator|new
name|StorageClusterStatusModel
argument_list|()
decl_stmt|;
return|return
operator|(
name|StorageClusterStatusModel
operator|)
name|s
operator|.
name|getObjectFromMessage
argument_list|(
name|response
operator|.
name|getBody
argument_list|()
argument_list|)
return|;
case|case
literal|404
case|:
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Cluster version not found"
argument_list|)
throw|;
case|case
literal|509
case|:
try|try
block|{
name|Thread
operator|.
name|sleep
argument_list|(
name|sleepTime
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
throw|throw
operator|(
name|InterruptedIOException
operator|)
operator|new
name|InterruptedIOException
argument_list|()
operator|.
name|initCause
argument_list|(
name|e
argument_list|)
throw|;
block|}
break|break;
default|default:
throw|throw
operator|new
name|IOException
argument_list|(
literal|"get request to "
operator|+
name|path
operator|+
literal|" returned "
operator|+
name|code
argument_list|)
throw|;
block|}
block|}
throw|throw
operator|new
name|IOException
argument_list|(
literal|"get request to "
operator|+
name|path
operator|+
literal|" timed out"
argument_list|)
throw|;
block|}
comment|/**    * @return string representing the cluster's version    * @throws IOEXception    *           if the endpoint does not exist, there is a timeout, or some other    *           general failure mode    */
specifier|public
name|StorageClusterVersionModel
name|getClusterVersion
parameter_list|()
throws|throws
name|IOException
block|{
name|StringBuilder
name|path
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|path
operator|.
name|append
argument_list|(
literal|'/'
argument_list|)
expr_stmt|;
if|if
condition|(
name|accessToken
operator|!=
literal|null
condition|)
block|{
name|path
operator|.
name|append
argument_list|(
name|accessToken
argument_list|)
expr_stmt|;
name|path
operator|.
name|append
argument_list|(
literal|'/'
argument_list|)
expr_stmt|;
block|}
name|path
operator|.
name|append
argument_list|(
literal|"version/cluster"
argument_list|)
expr_stmt|;
name|int
name|code
init|=
literal|0
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
name|maxRetries
condition|;
name|i
operator|++
control|)
block|{
name|Response
name|response
init|=
name|client
operator|.
name|get
argument_list|(
name|path
operator|.
name|toString
argument_list|()
argument_list|,
name|Constants
operator|.
name|MIMETYPE_XML
argument_list|)
decl_stmt|;
name|code
operator|=
name|response
operator|.
name|getCode
argument_list|()
expr_stmt|;
switch|switch
condition|(
name|code
condition|)
block|{
case|case
literal|200
case|:
try|try
block|{
return|return
operator|(
name|StorageClusterVersionModel
operator|)
name|getUnmarsheller
argument_list|()
operator|.
name|unmarshal
argument_list|(
operator|new
name|ByteArrayInputStream
argument_list|(
name|response
operator|.
name|getBody
argument_list|()
argument_list|)
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|JAXBException
name|jaxbe
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Issue parsing StorageClusterVersionModel object in XML form: "
operator|+
name|jaxbe
operator|.
name|getLocalizedMessage
argument_list|()
argument_list|)
throw|;
block|}
case|case
literal|404
case|:
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Cluster version not found"
argument_list|)
throw|;
case|case
literal|509
case|:
try|try
block|{
name|Thread
operator|.
name|sleep
argument_list|(
name|sleepTime
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
throw|throw
operator|(
name|InterruptedIOException
operator|)
operator|new
name|InterruptedIOException
argument_list|()
operator|.
name|initCause
argument_list|(
name|e
argument_list|)
throw|;
block|}
break|break;
default|default:
throw|throw
operator|new
name|IOException
argument_list|(
name|path
operator|.
name|toString
argument_list|()
operator|+
literal|" request returned "
operator|+
name|code
argument_list|)
throw|;
block|}
block|}
throw|throw
operator|new
name|IOException
argument_list|(
literal|"get request to "
operator|+
name|path
operator|.
name|toString
argument_list|()
operator|+
literal|" request timed out"
argument_list|)
throw|;
block|}
comment|/**    * @param tableName name of table to check    * @return true if all regions of the table are available    * @throws IOException if a remote or network exception occurs    */
specifier|public
name|boolean
name|isTableAvailable
parameter_list|(
name|byte
index|[]
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
name|StringBuilder
name|path
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|path
operator|.
name|append
argument_list|(
literal|'/'
argument_list|)
expr_stmt|;
if|if
condition|(
name|accessToken
operator|!=
literal|null
condition|)
block|{
name|path
operator|.
name|append
argument_list|(
name|accessToken
argument_list|)
expr_stmt|;
name|path
operator|.
name|append
argument_list|(
literal|'/'
argument_list|)
expr_stmt|;
block|}
name|path
operator|.
name|append
argument_list|(
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|tableName
argument_list|)
argument_list|)
expr_stmt|;
name|path
operator|.
name|append
argument_list|(
literal|'/'
argument_list|)
expr_stmt|;
name|path
operator|.
name|append
argument_list|(
literal|"exists"
argument_list|)
expr_stmt|;
name|int
name|code
init|=
literal|0
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
name|maxRetries
condition|;
name|i
operator|++
control|)
block|{
name|Response
name|response
init|=
name|client
operator|.
name|get
argument_list|(
name|path
operator|.
name|toString
argument_list|()
argument_list|,
name|Constants
operator|.
name|MIMETYPE_PROTOBUF
argument_list|)
decl_stmt|;
name|code
operator|=
name|response
operator|.
name|getCode
argument_list|()
expr_stmt|;
switch|switch
condition|(
name|code
condition|)
block|{
case|case
literal|200
case|:
return|return
literal|true
return|;
case|case
literal|404
case|:
return|return
literal|false
return|;
case|case
literal|509
case|:
try|try
block|{
name|Thread
operator|.
name|sleep
argument_list|(
name|sleepTime
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
throw|throw
operator|(
name|InterruptedIOException
operator|)
operator|new
name|InterruptedIOException
argument_list|()
operator|.
name|initCause
argument_list|(
name|e
argument_list|)
throw|;
block|}
break|break;
default|default:
throw|throw
operator|new
name|IOException
argument_list|(
literal|"get request to "
operator|+
name|path
operator|.
name|toString
argument_list|()
operator|+
literal|" returned "
operator|+
name|code
argument_list|)
throw|;
block|}
block|}
throw|throw
operator|new
name|IOException
argument_list|(
literal|"get request to "
operator|+
name|path
operator|.
name|toString
argument_list|()
operator|+
literal|" timed out"
argument_list|)
throw|;
block|}
comment|/**    * Creates a new table.    * @param desc table descriptor for table    * @throws IOException if a remote or network exception occurs    */
specifier|public
name|void
name|createTable
parameter_list|(
name|HTableDescriptor
name|desc
parameter_list|)
throws|throws
name|IOException
block|{
name|TableSchemaModel
name|model
init|=
operator|new
name|TableSchemaModel
argument_list|(
name|desc
argument_list|)
decl_stmt|;
name|StringBuilder
name|path
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|path
operator|.
name|append
argument_list|(
literal|'/'
argument_list|)
expr_stmt|;
if|if
condition|(
name|accessToken
operator|!=
literal|null
condition|)
block|{
name|path
operator|.
name|append
argument_list|(
name|accessToken
argument_list|)
expr_stmt|;
name|path
operator|.
name|append
argument_list|(
literal|'/'
argument_list|)
expr_stmt|;
block|}
name|path
operator|.
name|append
argument_list|(
name|desc
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
name|path
operator|.
name|append
argument_list|(
literal|'/'
argument_list|)
expr_stmt|;
name|path
operator|.
name|append
argument_list|(
literal|"schema"
argument_list|)
expr_stmt|;
name|int
name|code
init|=
literal|0
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
name|maxRetries
condition|;
name|i
operator|++
control|)
block|{
name|Response
name|response
init|=
name|client
operator|.
name|put
argument_list|(
name|path
operator|.
name|toString
argument_list|()
argument_list|,
name|Constants
operator|.
name|MIMETYPE_PROTOBUF
argument_list|,
name|model
operator|.
name|createProtobufOutput
argument_list|()
argument_list|)
decl_stmt|;
name|code
operator|=
name|response
operator|.
name|getCode
argument_list|()
expr_stmt|;
switch|switch
condition|(
name|code
condition|)
block|{
case|case
literal|201
case|:
return|return;
case|case
literal|509
case|:
try|try
block|{
name|Thread
operator|.
name|sleep
argument_list|(
name|sleepTime
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
throw|throw
operator|(
name|InterruptedIOException
operator|)
operator|new
name|InterruptedIOException
argument_list|()
operator|.
name|initCause
argument_list|(
name|e
argument_list|)
throw|;
block|}
break|break;
default|default:
throw|throw
operator|new
name|IOException
argument_list|(
literal|"create request to "
operator|+
name|path
operator|.
name|toString
argument_list|()
operator|+
literal|" returned "
operator|+
name|code
argument_list|)
throw|;
block|}
block|}
throw|throw
operator|new
name|IOException
argument_list|(
literal|"create request to "
operator|+
name|path
operator|.
name|toString
argument_list|()
operator|+
literal|" timed out"
argument_list|)
throw|;
block|}
comment|/**    * Deletes a table.    * @param tableName name of table to delete    * @throws IOException if a remote or network exception occurs    */
specifier|public
name|void
name|deleteTable
parameter_list|(
specifier|final
name|String
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
name|deleteTable
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|tableName
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Deletes a table.    * @param tableName name of table to delete    * @throws IOException if a remote or network exception occurs    */
specifier|public
name|void
name|deleteTable
parameter_list|(
specifier|final
name|byte
index|[]
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
name|StringBuilder
name|path
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|path
operator|.
name|append
argument_list|(
literal|'/'
argument_list|)
expr_stmt|;
if|if
condition|(
name|accessToken
operator|!=
literal|null
condition|)
block|{
name|path
operator|.
name|append
argument_list|(
name|accessToken
argument_list|)
expr_stmt|;
name|path
operator|.
name|append
argument_list|(
literal|'/'
argument_list|)
expr_stmt|;
block|}
name|path
operator|.
name|append
argument_list|(
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|tableName
argument_list|)
argument_list|)
expr_stmt|;
name|path
operator|.
name|append
argument_list|(
literal|'/'
argument_list|)
expr_stmt|;
name|path
operator|.
name|append
argument_list|(
literal|"schema"
argument_list|)
expr_stmt|;
name|int
name|code
init|=
literal|0
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
name|maxRetries
condition|;
name|i
operator|++
control|)
block|{
name|Response
name|response
init|=
name|client
operator|.
name|delete
argument_list|(
name|path
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
name|code
operator|=
name|response
operator|.
name|getCode
argument_list|()
expr_stmt|;
switch|switch
condition|(
name|code
condition|)
block|{
case|case
literal|200
case|:
return|return;
case|case
literal|509
case|:
try|try
block|{
name|Thread
operator|.
name|sleep
argument_list|(
name|sleepTime
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
throw|throw
operator|(
name|InterruptedIOException
operator|)
operator|new
name|InterruptedIOException
argument_list|()
operator|.
name|initCause
argument_list|(
name|e
argument_list|)
throw|;
block|}
break|break;
default|default:
throw|throw
operator|new
name|IOException
argument_list|(
literal|"delete request to "
operator|+
name|path
operator|.
name|toString
argument_list|()
operator|+
literal|" returned "
operator|+
name|code
argument_list|)
throw|;
block|}
block|}
throw|throw
operator|new
name|IOException
argument_list|(
literal|"delete request to "
operator|+
name|path
operator|.
name|toString
argument_list|()
operator|+
literal|" timed out"
argument_list|)
throw|;
block|}
comment|/**    * @return string representing the cluster's version    * @throws IOEXception    *           if the endpoint does not exist, there is a timeout, or some other    *           general failure mode    */
specifier|public
name|TableListModel
name|getTableList
parameter_list|()
throws|throws
name|IOException
block|{
name|StringBuilder
name|path
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|path
operator|.
name|append
argument_list|(
literal|'/'
argument_list|)
expr_stmt|;
if|if
condition|(
name|accessToken
operator|!=
literal|null
condition|)
block|{
name|path
operator|.
name|append
argument_list|(
name|accessToken
argument_list|)
expr_stmt|;
name|path
operator|.
name|append
argument_list|(
literal|'/'
argument_list|)
expr_stmt|;
block|}
name|int
name|code
init|=
literal|0
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
name|maxRetries
condition|;
name|i
operator|++
control|)
block|{
comment|// Response response = client.get(path.toString(),
comment|// Constants.MIMETYPE_XML);
name|Response
name|response
init|=
name|client
operator|.
name|get
argument_list|(
name|path
operator|.
name|toString
argument_list|()
argument_list|,
name|Constants
operator|.
name|MIMETYPE_PROTOBUF
argument_list|)
decl_stmt|;
name|code
operator|=
name|response
operator|.
name|getCode
argument_list|()
expr_stmt|;
switch|switch
condition|(
name|code
condition|)
block|{
case|case
literal|200
case|:
name|TableListModel
name|t
init|=
operator|new
name|TableListModel
argument_list|()
decl_stmt|;
return|return
operator|(
name|TableListModel
operator|)
name|t
operator|.
name|getObjectFromMessage
argument_list|(
name|response
operator|.
name|getBody
argument_list|()
argument_list|)
return|;
case|case
literal|404
case|:
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Table list not found"
argument_list|)
throw|;
case|case
literal|509
case|:
try|try
block|{
name|Thread
operator|.
name|sleep
argument_list|(
name|sleepTime
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
throw|throw
operator|(
name|InterruptedIOException
operator|)
operator|new
name|InterruptedIOException
argument_list|()
operator|.
name|initCause
argument_list|(
name|e
argument_list|)
throw|;
block|}
break|break;
default|default:
throw|throw
operator|new
name|IOException
argument_list|(
literal|"get request to "
operator|+
name|path
operator|.
name|toString
argument_list|()
operator|+
literal|" request returned "
operator|+
name|code
argument_list|)
throw|;
block|}
block|}
throw|throw
operator|new
name|IOException
argument_list|(
literal|"get request to "
operator|+
name|path
operator|.
name|toString
argument_list|()
operator|+
literal|" request timed out"
argument_list|)
throw|;
block|}
block|}
end_class

end_unit

