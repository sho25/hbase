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
name|client
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
name|hadoop
operator|.
name|hbase
operator|.
name|ipc
operator|.
name|RpcControllerFactory
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
name|hadoop
operator|.
name|hbase
operator|.
name|shaded
operator|.
name|protobuf
operator|.
name|ProtobufUtil
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
name|RequestConverter
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
name|ClientProtos
operator|.
name|BulkLoadHFileRequest
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
name|ClientProtos
operator|.
name|BulkLoadHFileResponse
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
name|ClientProtos
operator|.
name|CleanupBulkLoadRequest
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
name|ClientProtos
operator|.
name|ClientService
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
name|ClientProtos
operator|.
name|PrepareBulkLoadRequest
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
name|ClientProtos
operator|.
name|PrepareBulkLoadResponse
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
name|HBaseProtos
operator|.
name|RegionSpecifier
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
name|HBaseProtos
operator|.
name|RegionSpecifier
operator|.
name|RegionSpecifierType
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
name|security
operator|.
name|token
operator|.
name|Token
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
name|HConstants
operator|.
name|PRIORITY_UNSET
import|;
end_import

begin_comment
comment|/**  * Client proxy for SecureBulkLoadProtocol  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|SecureBulkLoadClient
block|{
specifier|private
name|Table
name|table
decl_stmt|;
specifier|private
specifier|final
name|RpcControllerFactory
name|rpcControllerFactory
decl_stmt|;
specifier|public
name|SecureBulkLoadClient
parameter_list|(
specifier|final
name|Configuration
name|conf
parameter_list|,
name|Table
name|table
parameter_list|)
block|{
name|this
operator|.
name|table
operator|=
name|table
expr_stmt|;
name|this
operator|.
name|rpcControllerFactory
operator|=
operator|new
name|RpcControllerFactory
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
specifier|public
name|String
name|prepareBulkLoad
parameter_list|(
specifier|final
name|Connection
name|conn
parameter_list|)
throws|throws
name|IOException
block|{
try|try
block|{
name|ClientServiceCallable
argument_list|<
name|String
argument_list|>
name|callable
init|=
operator|new
name|ClientServiceCallable
argument_list|<
name|String
argument_list|>
argument_list|(
name|conn
argument_list|,
name|table
operator|.
name|getName
argument_list|()
argument_list|,
name|HConstants
operator|.
name|EMPTY_START_ROW
argument_list|,
name|this
operator|.
name|rpcControllerFactory
operator|.
name|newController
argument_list|()
argument_list|,
name|PRIORITY_UNSET
argument_list|)
block|{
annotation|@
name|Override
specifier|protected
name|String
name|rpcCall
parameter_list|()
throws|throws
name|Exception
block|{
name|byte
index|[]
name|regionName
init|=
name|getLocation
argument_list|()
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getRegionName
argument_list|()
decl_stmt|;
name|RegionSpecifier
name|region
init|=
name|RequestConverter
operator|.
name|buildRegionSpecifier
argument_list|(
name|RegionSpecifierType
operator|.
name|REGION_NAME
argument_list|,
name|regionName
argument_list|)
decl_stmt|;
name|PrepareBulkLoadRequest
name|request
init|=
name|PrepareBulkLoadRequest
operator|.
name|newBuilder
argument_list|()
operator|.
name|setTableName
argument_list|(
name|ProtobufUtil
operator|.
name|toProtoTableName
argument_list|(
name|table
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|)
operator|.
name|setRegion
argument_list|(
name|region
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|PrepareBulkLoadResponse
name|response
init|=
name|getStub
argument_list|()
operator|.
name|prepareBulkLoad
argument_list|(
literal|null
argument_list|,
name|request
argument_list|)
decl_stmt|;
return|return
name|response
operator|.
name|getBulkToken
argument_list|()
return|;
block|}
block|}
decl_stmt|;
return|return
name|RpcRetryingCallerFactory
operator|.
name|instantiate
argument_list|(
name|conn
operator|.
name|getConfiguration
argument_list|()
argument_list|,
literal|null
argument_list|)
operator|.
operator|<
name|String
operator|>
name|newCaller
argument_list|()
operator|.
name|callWithRetries
argument_list|(
name|callable
argument_list|,
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|throwable
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|throwable
argument_list|)
throw|;
block|}
block|}
specifier|public
name|void
name|cleanupBulkLoad
parameter_list|(
specifier|final
name|Connection
name|conn
parameter_list|,
specifier|final
name|String
name|bulkToken
parameter_list|)
throws|throws
name|IOException
block|{
try|try
block|{
name|ClientServiceCallable
argument_list|<
name|Void
argument_list|>
name|callable
init|=
operator|new
name|ClientServiceCallable
argument_list|<
name|Void
argument_list|>
argument_list|(
name|conn
argument_list|,
name|table
operator|.
name|getName
argument_list|()
argument_list|,
name|HConstants
operator|.
name|EMPTY_START_ROW
argument_list|,
name|this
operator|.
name|rpcControllerFactory
operator|.
name|newController
argument_list|()
argument_list|,
name|PRIORITY_UNSET
argument_list|)
block|{
annotation|@
name|Override
specifier|protected
name|Void
name|rpcCall
parameter_list|()
throws|throws
name|Exception
block|{
name|byte
index|[]
name|regionName
init|=
name|getLocation
argument_list|()
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getRegionName
argument_list|()
decl_stmt|;
name|RegionSpecifier
name|region
init|=
name|RequestConverter
operator|.
name|buildRegionSpecifier
argument_list|(
name|RegionSpecifierType
operator|.
name|REGION_NAME
argument_list|,
name|regionName
argument_list|)
decl_stmt|;
name|CleanupBulkLoadRequest
name|request
init|=
name|CleanupBulkLoadRequest
operator|.
name|newBuilder
argument_list|()
operator|.
name|setRegion
argument_list|(
name|region
argument_list|)
operator|.
name|setBulkToken
argument_list|(
name|bulkToken
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|getStub
argument_list|()
operator|.
name|cleanupBulkLoad
argument_list|(
literal|null
argument_list|,
name|request
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
decl_stmt|;
name|RpcRetryingCallerFactory
operator|.
name|instantiate
argument_list|(
name|conn
operator|.
name|getConfiguration
argument_list|()
argument_list|,
literal|null
argument_list|)
operator|.
operator|<
name|Void
operator|>
name|newCaller
argument_list|()
operator|.
name|callWithRetries
argument_list|(
name|callable
argument_list|,
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|throwable
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|throwable
argument_list|)
throw|;
block|}
block|}
comment|/**    * Securely bulk load a list of HFiles using client protocol.    *    * @param client    * @param familyPaths    * @param regionName    * @param assignSeqNum    * @param userToken    * @param bulkToken    * @return true if all are loaded    * @throws IOException    */
specifier|public
name|boolean
name|secureBulkLoadHFiles
parameter_list|(
specifier|final
name|ClientService
operator|.
name|BlockingInterface
name|client
parameter_list|,
specifier|final
name|List
argument_list|<
name|Pair
argument_list|<
name|byte
index|[]
argument_list|,
name|String
argument_list|>
argument_list|>
name|familyPaths
parameter_list|,
specifier|final
name|byte
index|[]
name|regionName
parameter_list|,
name|boolean
name|assignSeqNum
parameter_list|,
specifier|final
name|Token
argument_list|<
name|?
argument_list|>
name|userToken
parameter_list|,
specifier|final
name|String
name|bulkToken
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|secureBulkLoadHFiles
argument_list|(
name|client
argument_list|,
name|familyPaths
argument_list|,
name|regionName
argument_list|,
name|assignSeqNum
argument_list|,
name|userToken
argument_list|,
name|bulkToken
argument_list|,
literal|false
argument_list|)
return|;
block|}
comment|/**    * Securely bulk load a list of HFiles using client protocol.    *    * @param client    * @param familyPaths    * @param regionName    * @param assignSeqNum    * @param userToken    * @param bulkToken    * @param copyFiles    * @return true if all are loaded    * @throws IOException    */
specifier|public
name|boolean
name|secureBulkLoadHFiles
parameter_list|(
specifier|final
name|ClientService
operator|.
name|BlockingInterface
name|client
parameter_list|,
specifier|final
name|List
argument_list|<
name|Pair
argument_list|<
name|byte
index|[]
argument_list|,
name|String
argument_list|>
argument_list|>
name|familyPaths
parameter_list|,
specifier|final
name|byte
index|[]
name|regionName
parameter_list|,
name|boolean
name|assignSeqNum
parameter_list|,
specifier|final
name|Token
argument_list|<
name|?
argument_list|>
name|userToken
parameter_list|,
specifier|final
name|String
name|bulkToken
parameter_list|,
name|boolean
name|copyFiles
parameter_list|)
throws|throws
name|IOException
block|{
name|BulkLoadHFileRequest
name|request
init|=
name|RequestConverter
operator|.
name|buildBulkLoadHFileRequest
argument_list|(
name|familyPaths
argument_list|,
name|regionName
argument_list|,
name|assignSeqNum
argument_list|,
name|userToken
argument_list|,
name|bulkToken
argument_list|,
name|copyFiles
argument_list|)
decl_stmt|;
try|try
block|{
name|BulkLoadHFileResponse
name|response
init|=
name|client
operator|.
name|bulkLoadHFile
argument_list|(
literal|null
argument_list|,
name|request
argument_list|)
decl_stmt|;
return|return
name|response
operator|.
name|getLoaded
argument_list|()
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|se
parameter_list|)
block|{
throw|throw
name|ProtobufUtil
operator|.
name|handleRemoteException
argument_list|(
name|se
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

