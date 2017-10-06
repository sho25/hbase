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
name|security
operator|.
name|access
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
name|Collections
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
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|Log
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
name|logging
operator|.
name|LogFactory
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
name|CoprocessorEnvironment
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
name|coprocessor
operator|.
name|RegionCoprocessor
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
name|coprocessor
operator|.
name|RegionCoprocessorEnvironment
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
name|CoprocessorRpcUtils
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
name|protobuf
operator|.
name|generated
operator|.
name|ClientProtos
operator|.
name|CleanupBulkLoadResponse
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
name|hbase
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|SecureBulkLoadProtos
operator|.
name|SecureBulkLoadHFilesRequest
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
operator|.
name|SecureBulkLoadHFilesResponse
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
operator|.
name|SecureBulkLoadService
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
name|regionserver
operator|.
name|HRegion
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
name|regionserver
operator|.
name|RegionServerServices
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
name|regionserver
operator|.
name|SecureBulkLoadManager
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|RpcCallback
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|RpcController
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|Service
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
comment|/**  * Coprocessor service for bulk loads in secure mode.  * @deprecated As of release 2.0.0, this will be removed in HBase 3.0.0  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
annotation|@
name|Deprecated
specifier|public
class|class
name|SecureBulkLoadEndpoint
extends|extends
name|SecureBulkLoadService
implements|implements
name|RegionCoprocessor
block|{
specifier|public
specifier|static
specifier|final
name|long
name|VERSION
init|=
literal|0L
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|SecureBulkLoadEndpoint
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|RegionCoprocessorEnvironment
name|env
decl_stmt|;
specifier|private
name|RegionServerServices
name|rsServices
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|start
parameter_list|(
name|CoprocessorEnvironment
name|env
parameter_list|)
block|{
name|this
operator|.
name|env
operator|=
operator|(
name|RegionCoprocessorEnvironment
operator|)
name|env
expr_stmt|;
assert|assert
name|this
operator|.
name|env
operator|.
name|getCoprocessorRegionServerServices
argument_list|()
operator|instanceof
name|RegionServerServices
assert|;
name|rsServices
operator|=
operator|(
name|RegionServerServices
operator|)
name|this
operator|.
name|env
operator|.
name|getCoprocessorRegionServerServices
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|warn
argument_list|(
literal|"SecureBulkLoadEndpoint is deprecated. It will be removed in future releases."
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|warn
argument_list|(
literal|"Secure bulk load has been integrated into HBase core."
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|stop
parameter_list|(
name|CoprocessorEnvironment
name|env
parameter_list|)
throws|throws
name|IOException
block|{   }
annotation|@
name|Override
specifier|public
name|void
name|prepareBulkLoad
parameter_list|(
name|RpcController
name|controller
parameter_list|,
name|PrepareBulkLoadRequest
name|request
parameter_list|,
name|RpcCallback
argument_list|<
name|PrepareBulkLoadResponse
argument_list|>
name|done
parameter_list|)
block|{
try|try
block|{
name|SecureBulkLoadManager
name|secureBulkLoadManager
init|=
name|this
operator|.
name|rsServices
operator|.
name|getSecureBulkLoadManager
argument_list|()
decl_stmt|;
name|String
name|bulkToken
init|=
name|secureBulkLoadManager
operator|.
name|prepareBulkLoad
argument_list|(
operator|(
name|HRegion
operator|)
name|this
operator|.
name|env
operator|.
name|getRegion
argument_list|()
argument_list|,
name|convert
argument_list|(
name|request
argument_list|)
argument_list|)
decl_stmt|;
name|done
operator|.
name|run
argument_list|(
name|PrepareBulkLoadResponse
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
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|CoprocessorRpcUtils
operator|.
name|setControllerException
argument_list|(
name|controller
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
name|done
operator|.
name|run
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
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
name|convert
parameter_list|(
name|PrepareBulkLoadRequest
name|request
parameter_list|)
throws|throws
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
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|InvalidProtocolBufferException
block|{
name|byte
index|[]
name|bytes
init|=
name|request
operator|.
name|toByteArray
argument_list|()
decl_stmt|;
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
operator|.
name|Builder
name|builder
init|=
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
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
name|builder
operator|.
name|mergeFrom
argument_list|(
name|bytes
argument_list|)
expr_stmt|;
return|return
name|builder
operator|.
name|build
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|cleanupBulkLoad
parameter_list|(
name|RpcController
name|controller
parameter_list|,
name|CleanupBulkLoadRequest
name|request
parameter_list|,
name|RpcCallback
argument_list|<
name|CleanupBulkLoadResponse
argument_list|>
name|done
parameter_list|)
block|{
try|try
block|{
name|SecureBulkLoadManager
name|secureBulkLoadManager
init|=
name|this
operator|.
name|rsServices
operator|.
name|getSecureBulkLoadManager
argument_list|()
decl_stmt|;
name|secureBulkLoadManager
operator|.
name|cleanupBulkLoad
argument_list|(
operator|(
name|HRegion
operator|)
name|this
operator|.
name|env
operator|.
name|getRegion
argument_list|()
argument_list|,
name|convert
argument_list|(
name|request
argument_list|)
argument_list|)
expr_stmt|;
name|done
operator|.
name|run
argument_list|(
name|CleanupBulkLoadResponse
operator|.
name|newBuilder
argument_list|()
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|CoprocessorRpcUtils
operator|.
name|setControllerException
argument_list|(
name|controller
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
name|done
operator|.
name|run
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
comment|/**    * Convert from CPEP protobuf 2.5 to internal protobuf 3.3.    * @throws org.apache.hadoop.hbase.shaded.com.google.protobuf.InvalidProtocolBufferException    */
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
name|convert
parameter_list|(
name|CleanupBulkLoadRequest
name|request
parameter_list|)
throws|throws
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
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|InvalidProtocolBufferException
block|{
name|byte
index|[]
name|bytes
init|=
name|request
operator|.
name|toByteArray
argument_list|()
decl_stmt|;
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
operator|.
name|Builder
name|builder
init|=
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
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
name|builder
operator|.
name|mergeFrom
argument_list|(
name|bytes
argument_list|)
expr_stmt|;
return|return
name|builder
operator|.
name|build
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|secureBulkLoadHFiles
parameter_list|(
name|RpcController
name|controller
parameter_list|,
name|SecureBulkLoadHFilesRequest
name|request
parameter_list|,
name|RpcCallback
argument_list|<
name|SecureBulkLoadHFilesResponse
argument_list|>
name|done
parameter_list|)
block|{
name|boolean
name|loaded
init|=
literal|false
decl_stmt|;
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|List
argument_list|<
name|Path
argument_list|>
argument_list|>
name|map
init|=
literal|null
decl_stmt|;
try|try
block|{
name|SecureBulkLoadManager
name|secureBulkLoadManager
init|=
name|this
operator|.
name|rsServices
operator|.
name|getSecureBulkLoadManager
argument_list|()
decl_stmt|;
name|BulkLoadHFileRequest
name|bulkLoadHFileRequest
init|=
name|ConvertSecureBulkLoadHFilesRequest
argument_list|(
name|request
argument_list|)
decl_stmt|;
name|map
operator|=
name|secureBulkLoadManager
operator|.
name|secureBulkLoadHFiles
argument_list|(
operator|(
name|HRegion
operator|)
name|this
operator|.
name|env
operator|.
name|getRegion
argument_list|()
argument_list|,
name|convert
argument_list|(
name|bulkLoadHFileRequest
argument_list|)
argument_list|)
expr_stmt|;
name|loaded
operator|=
name|map
operator|!=
literal|null
operator|&&
operator|!
name|map
operator|.
name|isEmpty
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|CoprocessorRpcUtils
operator|.
name|setControllerException
argument_list|(
name|controller
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
name|done
operator|.
name|run
argument_list|(
name|SecureBulkLoadHFilesResponse
operator|.
name|newBuilder
argument_list|()
operator|.
name|setLoaded
argument_list|(
name|loaded
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
block|}
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
name|convert
parameter_list|(
name|BulkLoadHFileRequest
name|request
parameter_list|)
throws|throws
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
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|InvalidProtocolBufferException
block|{
name|byte
index|[]
name|bytes
init|=
name|request
operator|.
name|toByteArray
argument_list|()
decl_stmt|;
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
operator|.
name|Builder
name|builder
init|=
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
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
name|builder
operator|.
name|mergeFrom
argument_list|(
name|bytes
argument_list|)
expr_stmt|;
return|return
name|builder
operator|.
name|build
argument_list|()
return|;
block|}
specifier|private
name|BulkLoadHFileRequest
name|ConvertSecureBulkLoadHFilesRequest
parameter_list|(
name|SecureBulkLoadHFilesRequest
name|request
parameter_list|)
block|{
name|BulkLoadHFileRequest
operator|.
name|Builder
name|bulkLoadHFileRequest
init|=
name|BulkLoadHFileRequest
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
name|RegionSpecifier
name|region
init|=
name|ProtobufUtil
operator|.
name|buildRegionSpecifier
argument_list|(
name|RegionSpecifierType
operator|.
name|REGION_NAME
argument_list|,
name|this
operator|.
name|env
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getRegionName
argument_list|()
argument_list|)
decl_stmt|;
name|bulkLoadHFileRequest
operator|.
name|setRegion
argument_list|(
name|region
argument_list|)
operator|.
name|setFsToken
argument_list|(
name|request
operator|.
name|getFsToken
argument_list|()
argument_list|)
operator|.
name|setBulkToken
argument_list|(
name|request
operator|.
name|getBulkToken
argument_list|()
argument_list|)
operator|.
name|setAssignSeqNum
argument_list|(
name|request
operator|.
name|getAssignSeqNum
argument_list|()
argument_list|)
operator|.
name|addAllFamilyPath
argument_list|(
name|request
operator|.
name|getFamilyPathList
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|bulkLoadHFileRequest
operator|.
name|build
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|Iterable
argument_list|<
name|Service
argument_list|>
name|getServices
parameter_list|()
block|{
return|return
name|Collections
operator|.
name|singleton
argument_list|(
name|this
argument_list|)
return|;
block|}
block|}
end_class

end_unit

