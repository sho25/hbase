begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|thrift2
operator|.
name|client
package|;
end_package

begin_import
import|import static
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|ipc
operator|.
name|RpcClient
operator|.
name|DEFAULT_SOCKET_TIMEOUT_CONNECT
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|ipc
operator|.
name|RpcClient
operator|.
name|SOCKET_TIMEOUT_CONNECT
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
name|lang
operator|.
name|reflect
operator|.
name|Constructor
import|;
end_import

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
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ExecutorService
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|lang3
operator|.
name|NotImplementedException
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
name|HConstants
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
name|client
operator|.
name|Admin
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
name|client
operator|.
name|BufferedMutator
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
name|client
operator|.
name|BufferedMutatorParams
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
name|client
operator|.
name|Connection
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
name|client
operator|.
name|RegionLocator
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
name|client
operator|.
name|Table
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
name|client
operator|.
name|TableBuilder
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
name|security
operator|.
name|User
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
name|thrift
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
name|thrift2
operator|.
name|generated
operator|.
name|THBaseService
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
name|Pair
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|http
operator|.
name|client
operator|.
name|HttpClient
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|http
operator|.
name|client
operator|.
name|config
operator|.
name|RequestConfig
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|http
operator|.
name|client
operator|.
name|utils
operator|.
name|HttpClientUtils
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|http
operator|.
name|impl
operator|.
name|client
operator|.
name|HttpClientBuilder
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|protocol
operator|.
name|TBinaryProtocol
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|protocol
operator|.
name|TCompactProtocol
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|protocol
operator|.
name|TProtocol
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|transport
operator|.
name|TFramedTransport
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|transport
operator|.
name|THttpClient
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|transport
operator|.
name|TSocket
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|transport
operator|.
name|TTransport
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|transport
operator|.
name|TTransportException
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

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Preconditions
import|;
end_import

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|ThriftConnection
implements|implements
name|Connection
block|{
specifier|private
name|Configuration
name|conf
decl_stmt|;
specifier|private
name|User
name|user
decl_stmt|;
comment|// For HTTP protocol
specifier|private
name|HttpClient
name|httpClient
decl_stmt|;
specifier|private
name|boolean
name|httpClientCreated
init|=
literal|false
decl_stmt|;
specifier|private
name|boolean
name|isClosed
init|=
literal|false
decl_stmt|;
specifier|private
name|String
name|host
decl_stmt|;
specifier|private
name|int
name|port
decl_stmt|;
specifier|private
name|boolean
name|isFramed
init|=
literal|false
decl_stmt|;
specifier|private
name|boolean
name|isCompact
init|=
literal|false
decl_stmt|;
specifier|private
name|ThriftClientBuilder
name|clientBuilder
decl_stmt|;
specifier|private
name|int
name|operationTimeout
decl_stmt|;
specifier|private
name|int
name|connectTimeout
decl_stmt|;
specifier|public
name|ThriftConnection
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|ExecutorService
name|pool
parameter_list|,
specifier|final
name|User
name|user
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|this
operator|.
name|user
operator|=
name|user
expr_stmt|;
name|this
operator|.
name|host
operator|=
name|conf
operator|.
name|get
argument_list|(
name|Constants
operator|.
name|HBASE_THRIFT_SERVER_NAME
argument_list|)
expr_stmt|;
name|this
operator|.
name|port
operator|=
name|conf
operator|.
name|getInt
argument_list|(
name|Constants
operator|.
name|HBASE_THRIFT_SERVER_PORT
argument_list|,
operator|-
literal|1
argument_list|)
expr_stmt|;
name|Preconditions
operator|.
name|checkArgument
argument_list|(
name|port
operator|>
literal|0
argument_list|)
expr_stmt|;
name|Preconditions
operator|.
name|checkArgument
argument_list|(
name|host
operator|!=
literal|null
argument_list|)
expr_stmt|;
name|this
operator|.
name|isFramed
operator|=
name|conf
operator|.
name|getBoolean
argument_list|(
name|Constants
operator|.
name|FRAMED_CONF_KEY
argument_list|,
name|Constants
operator|.
name|FRAMED_CONF_DEFAULT
argument_list|)
expr_stmt|;
name|this
operator|.
name|isCompact
operator|=
name|conf
operator|.
name|getBoolean
argument_list|(
name|Constants
operator|.
name|COMPACT_CONF_KEY
argument_list|,
name|Constants
operator|.
name|COMPACT_CONF_DEFAULT
argument_list|)
expr_stmt|;
name|this
operator|.
name|operationTimeout
operator|=
name|conf
operator|.
name|getInt
argument_list|(
name|HConstants
operator|.
name|HBASE_CLIENT_OPERATION_TIMEOUT
argument_list|,
name|HConstants
operator|.
name|DEFAULT_HBASE_CLIENT_OPERATION_TIMEOUT
argument_list|)
expr_stmt|;
name|this
operator|.
name|connectTimeout
operator|=
name|conf
operator|.
name|getInt
argument_list|(
name|SOCKET_TIMEOUT_CONNECT
argument_list|,
name|DEFAULT_SOCKET_TIMEOUT_CONNECT
argument_list|)
expr_stmt|;
name|String
name|className
init|=
name|conf
operator|.
name|get
argument_list|(
name|Constants
operator|.
name|HBASE_THRIFT_CLIENT_BUIDLER_CLASS
argument_list|,
name|DefaultThriftClientBuilder
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
try|try
block|{
name|Class
argument_list|<
name|?
argument_list|>
name|clazz
init|=
name|Class
operator|.
name|forName
argument_list|(
name|className
argument_list|)
decl_stmt|;
name|Constructor
argument_list|<
name|?
argument_list|>
name|constructor
init|=
name|clazz
operator|.
name|getDeclaredConstructor
argument_list|(
name|ThriftConnection
operator|.
name|class
argument_list|)
decl_stmt|;
name|constructor
operator|.
name|setAccessible
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|clientBuilder
operator|=
operator|(
name|ThriftClientBuilder
operator|)
name|constructor
operator|.
name|newInstance
argument_list|(
name|this
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
specifier|public
specifier|synchronized
name|void
name|setHttpClient
parameter_list|(
name|HttpClient
name|httpClient
parameter_list|)
block|{
name|this
operator|.
name|httpClient
operator|=
name|httpClient
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Configuration
name|getConfiguration
parameter_list|()
block|{
return|return
name|conf
return|;
block|}
specifier|public
name|String
name|getHost
parameter_list|()
block|{
return|return
name|host
return|;
block|}
specifier|public
name|int
name|getPort
parameter_list|()
block|{
return|return
name|port
return|;
block|}
specifier|public
name|boolean
name|isFramed
parameter_list|()
block|{
return|return
name|isFramed
return|;
block|}
specifier|public
name|boolean
name|isCompact
parameter_list|()
block|{
return|return
name|isCompact
return|;
block|}
specifier|public
name|int
name|getOperationTimeout
parameter_list|()
block|{
return|return
name|operationTimeout
return|;
block|}
specifier|public
name|int
name|getConnectTimeout
parameter_list|()
block|{
return|return
name|connectTimeout
return|;
block|}
specifier|public
name|ThriftClientBuilder
name|getClientBuilder
parameter_list|()
block|{
return|return
name|clientBuilder
return|;
block|}
comment|/**    * the default thrift client builder.    * One can extend the ThriftClientBuilder to builder custom client, implement    * features like authentication(hbase-examples/thrift/DemoClient)    *    */
specifier|public
specifier|static
class|class
name|DefaultThriftClientBuilder
extends|extends
name|ThriftClientBuilder
block|{
annotation|@
name|Override
specifier|public
name|Pair
argument_list|<
name|THBaseService
operator|.
name|Client
argument_list|,
name|TTransport
argument_list|>
name|getClient
parameter_list|()
throws|throws
name|IOException
block|{
name|TSocket
name|sock
init|=
operator|new
name|TSocket
argument_list|(
name|connection
operator|.
name|getHost
argument_list|()
argument_list|,
name|connection
operator|.
name|getPort
argument_list|()
argument_list|)
decl_stmt|;
name|sock
operator|.
name|setSocketTimeout
argument_list|(
name|connection
operator|.
name|getOperationTimeout
argument_list|()
argument_list|)
expr_stmt|;
name|sock
operator|.
name|setConnectTimeout
argument_list|(
name|connection
operator|.
name|getConnectTimeout
argument_list|()
argument_list|)
expr_stmt|;
name|TTransport
name|tTransport
init|=
name|sock
decl_stmt|;
if|if
condition|(
name|connection
operator|.
name|isFramed
argument_list|()
condition|)
block|{
name|tTransport
operator|=
operator|new
name|TFramedTransport
argument_list|(
name|tTransport
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|sock
operator|.
name|open
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|TTransportException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
name|TProtocol
name|prot
decl_stmt|;
if|if
condition|(
name|connection
operator|.
name|isCompact
argument_list|()
condition|)
block|{
name|prot
operator|=
operator|new
name|TCompactProtocol
argument_list|(
name|tTransport
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|prot
operator|=
operator|new
name|TBinaryProtocol
argument_list|(
name|tTransport
argument_list|)
expr_stmt|;
block|}
name|THBaseService
operator|.
name|Client
name|client
init|=
operator|new
name|THBaseService
operator|.
name|Client
argument_list|(
name|prot
argument_list|)
decl_stmt|;
return|return
operator|new
name|Pair
argument_list|<>
argument_list|(
name|client
argument_list|,
name|tTransport
argument_list|)
return|;
block|}
specifier|public
name|DefaultThriftClientBuilder
parameter_list|(
name|ThriftConnection
name|connection
parameter_list|)
block|{
name|super
argument_list|(
name|connection
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * the default thrift http client builder.    * One can extend the ThriftClientBuilder to builder custom http client, implement    * features like authentication or 'DoAs'(hbase-examples/thrift/HttpDoAsClient)    *    */
specifier|public
specifier|static
class|class
name|HTTPThriftClientBuilder
extends|extends
name|ThriftClientBuilder
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|customHeader
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
specifier|public
name|HTTPThriftClientBuilder
parameter_list|(
name|ThriftConnection
name|connection
parameter_list|)
block|{
name|super
argument_list|(
name|connection
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|addCostumHeader
parameter_list|(
name|String
name|key
parameter_list|,
name|String
name|value
parameter_list|)
block|{
name|customHeader
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Pair
argument_list|<
name|THBaseService
operator|.
name|Client
argument_list|,
name|TTransport
argument_list|>
name|getClient
parameter_list|()
throws|throws
name|IOException
block|{
name|Preconditions
operator|.
name|checkArgument
argument_list|(
name|connection
operator|.
name|getHost
argument_list|()
operator|.
name|startsWith
argument_list|(
literal|"http"
argument_list|)
argument_list|,
literal|"http client host must start with http or https"
argument_list|)
expr_stmt|;
name|String
name|url
init|=
name|connection
operator|.
name|getHost
argument_list|()
operator|+
literal|":"
operator|+
name|connection
operator|.
name|getPort
argument_list|()
decl_stmt|;
try|try
block|{
name|THttpClient
name|httpClient
init|=
operator|new
name|THttpClient
argument_list|(
name|url
argument_list|,
name|connection
operator|.
name|getHttpClient
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|header
range|:
name|customHeader
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|httpClient
operator|.
name|setCustomHeader
argument_list|(
name|header
operator|.
name|getKey
argument_list|()
argument_list|,
name|header
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|httpClient
operator|.
name|open
argument_list|()
expr_stmt|;
name|TProtocol
name|prot
init|=
operator|new
name|TBinaryProtocol
argument_list|(
name|httpClient
argument_list|)
decl_stmt|;
name|THBaseService
operator|.
name|Client
name|client
init|=
operator|new
name|THBaseService
operator|.
name|Client
argument_list|(
name|prot
argument_list|)
decl_stmt|;
return|return
operator|new
name|Pair
argument_list|<>
argument_list|(
name|client
argument_list|,
name|httpClient
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|TTransportException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
block|}
comment|/**    * Get a ThriftAdmin, ThriftAdmin is NOT thread safe    * @return a ThriftAdmin    * @throws IOException IOException    */
annotation|@
name|Override
specifier|public
name|Admin
name|getAdmin
parameter_list|()
throws|throws
name|IOException
block|{
name|Pair
argument_list|<
name|THBaseService
operator|.
name|Client
argument_list|,
name|TTransport
argument_list|>
name|client
init|=
name|clientBuilder
operator|.
name|getClient
argument_list|()
decl_stmt|;
return|return
operator|new
name|ThriftAdmin
argument_list|(
name|client
operator|.
name|getFirst
argument_list|()
argument_list|,
name|client
operator|.
name|getSecond
argument_list|()
argument_list|,
name|conf
argument_list|)
return|;
block|}
specifier|public
specifier|synchronized
name|HttpClient
name|getHttpClient
parameter_list|()
block|{
if|if
condition|(
name|httpClient
operator|!=
literal|null
condition|)
block|{
return|return
name|httpClient
return|;
block|}
name|HttpClientBuilder
name|builder
init|=
name|HttpClientBuilder
operator|.
name|create
argument_list|()
decl_stmt|;
name|RequestConfig
operator|.
name|Builder
name|requestBuilder
init|=
name|RequestConfig
operator|.
name|custom
argument_list|()
decl_stmt|;
name|requestBuilder
operator|=
name|requestBuilder
operator|.
name|setConnectTimeout
argument_list|(
name|getConnectTimeout
argument_list|()
argument_list|)
expr_stmt|;
name|requestBuilder
operator|=
name|requestBuilder
operator|.
name|setConnectionRequestTimeout
argument_list|(
name|getOperationTimeout
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|setDefaultRequestConfig
argument_list|(
name|requestBuilder
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|httpClient
operator|=
name|builder
operator|.
name|build
argument_list|()
expr_stmt|;
name|httpClientCreated
operator|=
literal|true
expr_stmt|;
return|return
name|httpClient
return|;
block|}
annotation|@
name|Override
specifier|public
specifier|synchronized
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|httpClient
operator|!=
literal|null
operator|&&
name|httpClientCreated
condition|)
block|{
name|HttpClientUtils
operator|.
name|closeQuietly
argument_list|(
name|httpClient
argument_list|)
expr_stmt|;
block|}
name|isClosed
operator|=
literal|true
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isClosed
parameter_list|()
block|{
return|return
name|isClosed
return|;
block|}
comment|/**    * Get a TableBuider to build ThriftTable, ThriftTable is NOT thread safe    * @return a TableBuilder    * @throws IOException IOException    */
annotation|@
name|Override
specifier|public
name|TableBuilder
name|getTableBuilder
parameter_list|(
name|TableName
name|tableName
parameter_list|,
name|ExecutorService
name|pool
parameter_list|)
block|{
return|return
operator|new
name|TableBuilder
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|TableBuilder
name|setOperationTimeout
parameter_list|(
name|int
name|timeout
parameter_list|)
block|{
return|return
name|this
return|;
block|}
annotation|@
name|Override
specifier|public
name|TableBuilder
name|setRpcTimeout
parameter_list|(
name|int
name|timeout
parameter_list|)
block|{
return|return
name|this
return|;
block|}
annotation|@
name|Override
specifier|public
name|TableBuilder
name|setReadRpcTimeout
parameter_list|(
name|int
name|timeout
parameter_list|)
block|{
return|return
name|this
return|;
block|}
annotation|@
name|Override
specifier|public
name|TableBuilder
name|setWriteRpcTimeout
parameter_list|(
name|int
name|timeout
parameter_list|)
block|{
return|return
name|this
return|;
block|}
annotation|@
name|Override
specifier|public
name|Table
name|build
parameter_list|()
block|{
try|try
block|{
name|Pair
argument_list|<
name|THBaseService
operator|.
name|Client
argument_list|,
name|TTransport
argument_list|>
name|client
init|=
name|clientBuilder
operator|.
name|getClient
argument_list|()
decl_stmt|;
return|return
operator|new
name|ThriftTable
argument_list|(
name|tableName
argument_list|,
name|client
operator|.
name|getFirst
argument_list|()
argument_list|,
name|client
operator|.
name|getSecond
argument_list|()
argument_list|,
name|conf
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ioE
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|ioE
argument_list|)
throw|;
block|}
block|}
block|}
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|abort
parameter_list|(
name|String
name|why
parameter_list|,
name|Throwable
name|e
parameter_list|)
block|{    }
annotation|@
name|Override
specifier|public
name|boolean
name|isAborted
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|BufferedMutator
name|getBufferedMutator
parameter_list|(
name|TableName
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
throw|throw
operator|new
name|NotImplementedException
argument_list|(
literal|"batchCoprocessorService not supported in ThriftTable"
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|BufferedMutator
name|getBufferedMutator
parameter_list|(
name|BufferedMutatorParams
name|params
parameter_list|)
throws|throws
name|IOException
block|{
throw|throw
operator|new
name|NotImplementedException
argument_list|(
literal|"batchCoprocessorService not supported in ThriftTable"
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|RegionLocator
name|getRegionLocator
parameter_list|(
name|TableName
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
throw|throw
operator|new
name|NotImplementedException
argument_list|(
literal|"batchCoprocessorService not supported in ThriftTable"
argument_list|)
throw|;
block|}
block|}
end_class

end_unit

