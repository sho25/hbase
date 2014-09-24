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
operator|.
name|coprocessor
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
name|HConstants
operator|.
name|EMPTY_START_ROW
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
name|LAST_ROW
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
name|util
operator|.
name|ByteStringer
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
name|ipc
operator|.
name|BlockingRpcCallback
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
name|CoprocessorRpcChannel
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
name|ServerRpcController
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
name|protobuf
operator|.
name|generated
operator|.
name|ClientProtos
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
name|protobuf
operator|.
name|generated
operator|.
name|SecureBulkLoadProtos
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
name|SecureBulkLoadUtil
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
name|security
operator|.
name|token
operator|.
name|Token
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
name|util
operator|.
name|ArrayList
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

begin_comment
comment|/**  * Client proxy for SecureBulkLoadProtocol  * used in conjunction with SecureBulkLoadEndpoint  */
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
specifier|public
name|SecureBulkLoadClient
parameter_list|(
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
block|}
specifier|public
name|String
name|prepareBulkLoad
parameter_list|(
specifier|final
name|TableName
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
try|try
block|{
return|return
name|table
operator|.
name|coprocessorService
argument_list|(
name|SecureBulkLoadProtos
operator|.
name|SecureBulkLoadService
operator|.
name|class
argument_list|,
name|EMPTY_START_ROW
argument_list|,
name|LAST_ROW
argument_list|,
operator|new
name|Batch
operator|.
name|Call
argument_list|<
name|SecureBulkLoadProtos
operator|.
name|SecureBulkLoadService
argument_list|,
name|String
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|String
name|call
parameter_list|(
name|SecureBulkLoadProtos
operator|.
name|SecureBulkLoadService
name|instance
parameter_list|)
throws|throws
name|IOException
block|{
name|ServerRpcController
name|controller
init|=
operator|new
name|ServerRpcController
argument_list|()
decl_stmt|;
name|BlockingRpcCallback
argument_list|<
name|SecureBulkLoadProtos
operator|.
name|PrepareBulkLoadResponse
argument_list|>
name|rpcCallback
init|=
operator|new
name|BlockingRpcCallback
argument_list|<
name|SecureBulkLoadProtos
operator|.
name|PrepareBulkLoadResponse
argument_list|>
argument_list|()
decl_stmt|;
name|SecureBulkLoadProtos
operator|.
name|PrepareBulkLoadRequest
name|request
init|=
name|SecureBulkLoadProtos
operator|.
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
name|tableName
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|instance
operator|.
name|prepareBulkLoad
argument_list|(
name|controller
argument_list|,
name|request
argument_list|,
name|rpcCallback
argument_list|)
expr_stmt|;
name|SecureBulkLoadProtos
operator|.
name|PrepareBulkLoadResponse
name|response
init|=
name|rpcCallback
operator|.
name|get
argument_list|()
decl_stmt|;
if|if
condition|(
name|controller
operator|.
name|failedOnException
argument_list|()
condition|)
block|{
throw|throw
name|controller
operator|.
name|getFailedOn
argument_list|()
throw|;
block|}
return|return
name|response
operator|.
name|getBulkToken
argument_list|()
return|;
block|}
block|}
argument_list|)
operator|.
name|entrySet
argument_list|()
operator|.
name|iterator
argument_list|()
operator|.
name|next
argument_list|()
operator|.
name|getValue
argument_list|()
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
name|String
name|bulkToken
parameter_list|)
throws|throws
name|IOException
block|{
try|try
block|{
name|table
operator|.
name|coprocessorService
argument_list|(
name|SecureBulkLoadProtos
operator|.
name|SecureBulkLoadService
operator|.
name|class
argument_list|,
name|EMPTY_START_ROW
argument_list|,
name|LAST_ROW
argument_list|,
operator|new
name|Batch
operator|.
name|Call
argument_list|<
name|SecureBulkLoadProtos
operator|.
name|SecureBulkLoadService
argument_list|,
name|String
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|String
name|call
parameter_list|(
name|SecureBulkLoadProtos
operator|.
name|SecureBulkLoadService
name|instance
parameter_list|)
throws|throws
name|IOException
block|{
name|ServerRpcController
name|controller
init|=
operator|new
name|ServerRpcController
argument_list|()
decl_stmt|;
name|BlockingRpcCallback
argument_list|<
name|SecureBulkLoadProtos
operator|.
name|CleanupBulkLoadResponse
argument_list|>
name|rpcCallback
init|=
operator|new
name|BlockingRpcCallback
argument_list|<
name|SecureBulkLoadProtos
operator|.
name|CleanupBulkLoadResponse
argument_list|>
argument_list|()
decl_stmt|;
name|SecureBulkLoadProtos
operator|.
name|CleanupBulkLoadRequest
name|request
init|=
name|SecureBulkLoadProtos
operator|.
name|CleanupBulkLoadRequest
operator|.
name|newBuilder
argument_list|()
operator|.
name|setBulkToken
argument_list|(
name|bulkToken
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|instance
operator|.
name|cleanupBulkLoad
argument_list|(
name|controller
argument_list|,
name|request
argument_list|,
name|rpcCallback
argument_list|)
expr_stmt|;
if|if
condition|(
name|controller
operator|.
name|failedOnException
argument_list|()
condition|)
block|{
throw|throw
name|controller
operator|.
name|getFailedOn
argument_list|()
throw|;
block|}
return|return
literal|null
return|;
block|}
block|}
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
specifier|public
name|boolean
name|bulkLoadHFiles
parameter_list|(
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
specifier|final
name|byte
index|[]
name|startRow
parameter_list|)
throws|throws
name|IOException
block|{
comment|// we never want to send a batch of HFiles to all regions, thus cannot call
comment|// HTable#coprocessorService methods that take start and end rowkeys; see HBASE-9639
try|try
block|{
name|CoprocessorRpcChannel
name|channel
init|=
name|table
operator|.
name|coprocessorService
argument_list|(
name|startRow
argument_list|)
decl_stmt|;
name|SecureBulkLoadProtos
operator|.
name|SecureBulkLoadService
name|instance
init|=
name|ProtobufUtil
operator|.
name|newServiceStub
argument_list|(
name|SecureBulkLoadProtos
operator|.
name|SecureBulkLoadService
operator|.
name|class
argument_list|,
name|channel
argument_list|)
decl_stmt|;
name|SecureBulkLoadProtos
operator|.
name|DelegationToken
name|protoDT
init|=
name|SecureBulkLoadProtos
operator|.
name|DelegationToken
operator|.
name|newBuilder
argument_list|()
operator|.
name|build
argument_list|()
decl_stmt|;
if|if
condition|(
name|userToken
operator|!=
literal|null
condition|)
block|{
name|protoDT
operator|=
name|SecureBulkLoadProtos
operator|.
name|DelegationToken
operator|.
name|newBuilder
argument_list|()
operator|.
name|setIdentifier
argument_list|(
name|ByteStringer
operator|.
name|wrap
argument_list|(
name|userToken
operator|.
name|getIdentifier
argument_list|()
argument_list|)
argument_list|)
operator|.
name|setPassword
argument_list|(
name|ByteStringer
operator|.
name|wrap
argument_list|(
name|userToken
operator|.
name|getPassword
argument_list|()
argument_list|)
argument_list|)
operator|.
name|setKind
argument_list|(
name|userToken
operator|.
name|getKind
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
operator|.
name|setService
argument_list|(
name|userToken
operator|.
name|getService
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
block|}
name|List
argument_list|<
name|ClientProtos
operator|.
name|BulkLoadHFileRequest
operator|.
name|FamilyPath
argument_list|>
name|protoFamilyPaths
init|=
operator|new
name|ArrayList
argument_list|<
name|ClientProtos
operator|.
name|BulkLoadHFileRequest
operator|.
name|FamilyPath
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|Pair
argument_list|<
name|byte
index|[]
argument_list|,
name|String
argument_list|>
name|el
range|:
name|familyPaths
control|)
block|{
name|protoFamilyPaths
operator|.
name|add
argument_list|(
name|ClientProtos
operator|.
name|BulkLoadHFileRequest
operator|.
name|FamilyPath
operator|.
name|newBuilder
argument_list|()
operator|.
name|setFamily
argument_list|(
name|ByteStringer
operator|.
name|wrap
argument_list|(
name|el
operator|.
name|getFirst
argument_list|()
argument_list|)
argument_list|)
operator|.
name|setPath
argument_list|(
name|el
operator|.
name|getSecond
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|SecureBulkLoadProtos
operator|.
name|SecureBulkLoadHFilesRequest
name|request
init|=
name|SecureBulkLoadProtos
operator|.
name|SecureBulkLoadHFilesRequest
operator|.
name|newBuilder
argument_list|()
operator|.
name|setFsToken
argument_list|(
name|protoDT
argument_list|)
operator|.
name|addAllFamilyPath
argument_list|(
name|protoFamilyPaths
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
name|ServerRpcController
name|controller
init|=
operator|new
name|ServerRpcController
argument_list|()
decl_stmt|;
name|BlockingRpcCallback
argument_list|<
name|SecureBulkLoadProtos
operator|.
name|SecureBulkLoadHFilesResponse
argument_list|>
name|rpcCallback
init|=
operator|new
name|BlockingRpcCallback
argument_list|<
name|SecureBulkLoadProtos
operator|.
name|SecureBulkLoadHFilesResponse
argument_list|>
argument_list|()
decl_stmt|;
name|instance
operator|.
name|secureBulkLoadHFiles
argument_list|(
name|controller
argument_list|,
name|request
argument_list|,
name|rpcCallback
argument_list|)
expr_stmt|;
name|SecureBulkLoadProtos
operator|.
name|SecureBulkLoadHFilesResponse
name|response
init|=
name|rpcCallback
operator|.
name|get
argument_list|()
decl_stmt|;
if|if
condition|(
name|controller
operator|.
name|failedOnException
argument_list|()
condition|)
block|{
throw|throw
name|controller
operator|.
name|getFailedOn
argument_list|()
throw|;
block|}
return|return
name|response
operator|.
name|getLoaded
argument_list|()
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
name|Path
name|getStagingPath
parameter_list|(
name|String
name|bulkToken
parameter_list|,
name|byte
index|[]
name|family
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|SecureBulkLoadUtil
operator|.
name|getStagingPath
argument_list|(
name|table
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|bulkToken
argument_list|,
name|family
argument_list|)
return|;
block|}
block|}
end_class

end_unit

