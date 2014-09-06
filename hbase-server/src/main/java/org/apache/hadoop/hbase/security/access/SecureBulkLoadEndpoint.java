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
name|FileStatus
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
name|FileSystem
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
name|FileUtil
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
name|fs
operator|.
name|permission
operator|.
name|FsPermission
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
name|Coprocessor
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
name|DoNotRetryIOException
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
name|CoprocessorService
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
name|RequestContext
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
name|ResponseConverter
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
name|protobuf
operator|.
name|generated
operator|.
name|SecureBulkLoadProtos
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
name|SecureBulkLoadProtos
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
name|SecureBulkLoadProtos
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
name|SecureBulkLoadProtos
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
name|security
operator|.
name|UserProvider
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
name|token
operator|.
name|FsDelegationToken
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
name|FSHDFSUtils
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
name|Methods
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
name|io
operator|.
name|Text
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
name|UserGroupInformation
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
name|math
operator|.
name|BigInteger
import|;
end_import

begin_import
import|import
name|java
operator|.
name|security
operator|.
name|PrivilegedAction
import|;
end_import

begin_import
import|import
name|java
operator|.
name|security
operator|.
name|SecureRandom
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
comment|/**  * Coprocessor service for bulk loads in secure mode.  * This coprocessor has to be installed as part of enabling  * security in HBase.  *  * This service addresses two issues:  *  * 1. Moving files in a secure filesystem wherein the HBase Client  * and HBase Server are different filesystem users.  * 2. Does moving in a secure manner. Assuming that the filesystem  * is POSIX compliant.  *  * The algorithm is as follows:  *  * 1. Create an hbase owned staging directory which is  * world traversable (711): /hbase/staging  * 2. A user writes out data to his secure output directory: /user/foo/data  * 3. A call is made to hbase to create a secret staging directory  * which globally rwx (777): /user/staging/averylongandrandomdirectoryname  * 4. The user moves the data into the random staging directory,  * then calls bulkLoadHFiles()  *  * Like delegation tokens the strength of the security lies in the length  * and randomness of the secret directory.  *  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|SecureBulkLoadEndpoint
extends|extends
name|SecureBulkLoadService
implements|implements
name|CoprocessorService
implements|,
name|Coprocessor
block|{
specifier|public
specifier|static
specifier|final
name|long
name|VERSION
init|=
literal|0L
decl_stmt|;
comment|//320/5 = 64 characters
specifier|private
specifier|static
specifier|final
name|int
name|RANDOM_WIDTH
init|=
literal|320
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|RANDOM_RADIX
init|=
literal|32
decl_stmt|;
specifier|private
specifier|static
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
specifier|final
specifier|static
name|FsPermission
name|PERM_ALL_ACCESS
init|=
name|FsPermission
operator|.
name|valueOf
argument_list|(
literal|"-rwxrwxrwx"
argument_list|)
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|FsPermission
name|PERM_HIDDEN
init|=
name|FsPermission
operator|.
name|valueOf
argument_list|(
literal|"-rwx--x--x"
argument_list|)
decl_stmt|;
specifier|private
name|SecureRandom
name|random
decl_stmt|;
specifier|private
name|FileSystem
name|fs
decl_stmt|;
specifier|private
name|Configuration
name|conf
decl_stmt|;
comment|//two levels so it doesn't get deleted accidentally
comment|//no sticky bit in Hadoop 1.0
specifier|private
name|Path
name|baseStagingDir
decl_stmt|;
specifier|private
name|RegionCoprocessorEnvironment
name|env
decl_stmt|;
specifier|private
name|UserProvider
name|userProvider
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
name|random
operator|=
operator|new
name|SecureRandom
argument_list|()
expr_stmt|;
name|conf
operator|=
name|env
operator|.
name|getConfiguration
argument_list|()
expr_stmt|;
name|baseStagingDir
operator|=
name|SecureBulkLoadUtil
operator|.
name|getBaseStagingDir
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|this
operator|.
name|userProvider
operator|=
name|UserProvider
operator|.
name|instantiate
argument_list|(
name|conf
argument_list|)
expr_stmt|;
try|try
block|{
name|fs
operator|=
name|FileSystem
operator|.
name|get
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|fs
operator|.
name|mkdirs
argument_list|(
name|baseStagingDir
argument_list|,
name|PERM_HIDDEN
argument_list|)
expr_stmt|;
name|fs
operator|.
name|setPermission
argument_list|(
name|baseStagingDir
argument_list|,
name|PERM_HIDDEN
argument_list|)
expr_stmt|;
comment|//no sticky bit in hadoop-1.0, making directory nonempty so it never gets erased
name|fs
operator|.
name|mkdirs
argument_list|(
operator|new
name|Path
argument_list|(
name|baseStagingDir
argument_list|,
literal|"DONOTERASE"
argument_list|)
argument_list|,
name|PERM_HIDDEN
argument_list|)
expr_stmt|;
name|FileStatus
name|status
init|=
name|fs
operator|.
name|getFileStatus
argument_list|(
name|baseStagingDir
argument_list|)
decl_stmt|;
if|if
condition|(
name|status
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Failed to create staging directory"
argument_list|)
throw|;
block|}
if|if
condition|(
operator|!
name|status
operator|.
name|getPermission
argument_list|()
operator|.
name|equals
argument_list|(
name|PERM_HIDDEN
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Directory already exists but permissions aren't set to '-rwx--x--x' "
argument_list|)
throw|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Failed to get FileSystem instance"
argument_list|,
name|e
argument_list|)
throw|;
block|}
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
name|getAccessController
argument_list|()
operator|.
name|prePrepareBulkLoad
argument_list|(
name|env
argument_list|)
expr_stmt|;
name|String
name|bulkToken
init|=
name|createStagingDir
argument_list|(
name|baseStagingDir
argument_list|,
name|getActiveUser
argument_list|()
argument_list|,
name|ProtobufUtil
operator|.
name|toTableName
argument_list|(
name|request
operator|.
name|getTableName
argument_list|()
argument_list|)
argument_list|)
operator|.
name|toString
argument_list|()
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
name|ResponseConverter
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
name|getAccessController
argument_list|()
operator|.
name|preCleanupBulkLoad
argument_list|(
name|env
argument_list|)
expr_stmt|;
name|fs
operator|.
name|delete
argument_list|(
name|createStagingDir
argument_list|(
name|baseStagingDir
argument_list|,
name|getActiveUser
argument_list|()
argument_list|,
operator|new
name|Path
argument_list|(
name|request
operator|.
name|getBulkToken
argument_list|()
argument_list|)
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|,
literal|true
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
name|ResponseConverter
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
init|=
operator|new
name|ArrayList
argument_list|<
name|Pair
argument_list|<
name|byte
index|[]
argument_list|,
name|String
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|ClientProtos
operator|.
name|BulkLoadHFileRequest
operator|.
name|FamilyPath
name|el
range|:
name|request
operator|.
name|getFamilyPathList
argument_list|()
control|)
block|{
name|familyPaths
operator|.
name|add
argument_list|(
operator|new
name|Pair
argument_list|(
name|el
operator|.
name|getFamily
argument_list|()
operator|.
name|toByteArray
argument_list|()
argument_list|,
name|el
operator|.
name|getPath
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|final
name|Token
name|userToken
init|=
operator|new
name|Token
argument_list|(
name|request
operator|.
name|getFsToken
argument_list|()
operator|.
name|getIdentifier
argument_list|()
operator|.
name|toByteArray
argument_list|()
argument_list|,
name|request
operator|.
name|getFsToken
argument_list|()
operator|.
name|getPassword
argument_list|()
operator|.
name|toByteArray
argument_list|()
argument_list|,
operator|new
name|Text
argument_list|(
name|request
operator|.
name|getFsToken
argument_list|()
operator|.
name|getKind
argument_list|()
argument_list|)
argument_list|,
operator|new
name|Text
argument_list|(
name|request
operator|.
name|getFsToken
argument_list|()
operator|.
name|getService
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
specifier|final
name|String
name|bulkToken
init|=
name|request
operator|.
name|getBulkToken
argument_list|()
decl_stmt|;
name|User
name|user
init|=
name|getActiveUser
argument_list|()
decl_stmt|;
specifier|final
name|UserGroupInformation
name|ugi
init|=
name|user
operator|.
name|getUGI
argument_list|()
decl_stmt|;
if|if
condition|(
name|userToken
operator|!=
literal|null
condition|)
block|{
name|ugi
operator|.
name|addToken
argument_list|(
name|userToken
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|userProvider
operator|.
name|isHadoopSecurityEnabled
argument_list|()
condition|)
block|{
comment|//we allow this to pass through in "simple" security mode
comment|//for mini cluster testing
name|ResponseConverter
operator|.
name|setControllerException
argument_list|(
name|controller
argument_list|,
operator|new
name|DoNotRetryIOException
argument_list|(
literal|"User token cannot be null"
argument_list|)
argument_list|)
expr_stmt|;
return|return;
block|}
name|HRegion
name|region
init|=
name|env
operator|.
name|getRegion
argument_list|()
decl_stmt|;
name|boolean
name|bypass
init|=
literal|false
decl_stmt|;
if|if
condition|(
name|region
operator|.
name|getCoprocessorHost
argument_list|()
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|bypass
operator|=
name|region
operator|.
name|getCoprocessorHost
argument_list|()
operator|.
name|preBulkLoadHFile
argument_list|(
name|familyPaths
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|ResponseConverter
operator|.
name|setControllerException
argument_list|(
name|controller
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|done
operator|.
name|run
argument_list|(
literal|null
argument_list|)
expr_stmt|;
return|return;
block|}
block|}
name|boolean
name|loaded
init|=
literal|false
decl_stmt|;
if|if
condition|(
operator|!
name|bypass
condition|)
block|{
comment|// Get the target fs (HBase region server fs) delegation token
comment|// Since we have checked the permission via 'preBulkLoadHFile', now let's give
comment|// the 'request user' necessary token to operate on the target fs.
comment|// After this point the 'doAs' user will hold two tokens, one for the source fs
comment|// ('request user'), another for the target fs (HBase region server principal).
name|FsDelegationToken
name|targetfsDelegationToken
init|=
operator|new
name|FsDelegationToken
argument_list|(
name|userProvider
argument_list|,
literal|"renewer"
argument_list|)
decl_stmt|;
try|try
block|{
name|targetfsDelegationToken
operator|.
name|acquireDelegationToken
argument_list|(
name|fs
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|ResponseConverter
operator|.
name|setControllerException
argument_list|(
name|controller
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|done
operator|.
name|run
argument_list|(
literal|null
argument_list|)
expr_stmt|;
return|return;
block|}
name|Token
argument_list|<
name|?
argument_list|>
name|targetFsToken
init|=
name|targetfsDelegationToken
operator|.
name|getUserToken
argument_list|()
decl_stmt|;
if|if
condition|(
name|targetFsToken
operator|!=
literal|null
operator|&&
operator|(
name|userToken
operator|==
literal|null
operator|||
operator|!
name|targetFsToken
operator|.
name|getService
argument_list|()
operator|.
name|equals
argument_list|(
name|userToken
operator|.
name|getService
argument_list|()
argument_list|)
operator|)
condition|)
block|{
name|ugi
operator|.
name|addToken
argument_list|(
name|targetFsToken
argument_list|)
expr_stmt|;
block|}
name|loaded
operator|=
name|ugi
operator|.
name|doAs
argument_list|(
operator|new
name|PrivilegedAction
argument_list|<
name|Boolean
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Boolean
name|run
parameter_list|()
block|{
name|FileSystem
name|fs
init|=
literal|null
decl_stmt|;
try|try
block|{
name|Configuration
name|conf
init|=
name|env
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|fs
operator|=
name|FileSystem
operator|.
name|get
argument_list|(
name|conf
argument_list|)
expr_stmt|;
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
name|Path
name|p
init|=
operator|new
name|Path
argument_list|(
name|el
operator|.
name|getSecond
argument_list|()
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|trace
argument_list|(
literal|"Setting permission for: "
operator|+
name|p
argument_list|)
expr_stmt|;
name|fs
operator|.
name|setPermission
argument_list|(
name|p
argument_list|,
name|PERM_ALL_ACCESS
argument_list|)
expr_stmt|;
name|Path
name|stageFamily
init|=
operator|new
name|Path
argument_list|(
name|bulkToken
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|el
operator|.
name|getFirst
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|fs
operator|.
name|exists
argument_list|(
name|stageFamily
argument_list|)
condition|)
block|{
name|fs
operator|.
name|mkdirs
argument_list|(
name|stageFamily
argument_list|)
expr_stmt|;
name|fs
operator|.
name|setPermission
argument_list|(
name|stageFamily
argument_list|,
name|PERM_ALL_ACCESS
argument_list|)
expr_stmt|;
block|}
block|}
comment|//We call bulkLoadHFiles as requesting user
comment|//To enable access prior to staging
return|return
name|env
operator|.
name|getRegion
argument_list|()
operator|.
name|bulkLoadHFiles
argument_list|(
name|familyPaths
argument_list|,
literal|true
argument_list|,
operator|new
name|SecureBulkLoadListener
argument_list|(
name|fs
argument_list|,
name|bulkToken
argument_list|,
name|conf
argument_list|)
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Failed to complete bulk load"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
return|return
literal|false
return|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|region
operator|.
name|getCoprocessorHost
argument_list|()
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|loaded
operator|=
name|region
operator|.
name|getCoprocessorHost
argument_list|()
operator|.
name|postBulkLoadHFile
argument_list|(
name|familyPaths
argument_list|,
name|loaded
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|ResponseConverter
operator|.
name|setControllerException
argument_list|(
name|controller
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|done
operator|.
name|run
argument_list|(
literal|null
argument_list|)
expr_stmt|;
return|return;
block|}
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
specifier|private
name|AccessController
name|getAccessController
parameter_list|()
block|{
return|return
operator|(
name|AccessController
operator|)
name|this
operator|.
name|env
operator|.
name|getRegion
argument_list|()
operator|.
name|getCoprocessorHost
argument_list|()
operator|.
name|findCoprocessor
argument_list|(
name|AccessController
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
return|;
block|}
specifier|private
name|Path
name|createStagingDir
parameter_list|(
name|Path
name|baseDir
parameter_list|,
name|User
name|user
parameter_list|,
name|TableName
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|tblName
init|=
name|tableName
operator|.
name|getNameAsString
argument_list|()
operator|.
name|replace
argument_list|(
literal|":"
argument_list|,
literal|"_"
argument_list|)
decl_stmt|;
name|String
name|randomDir
init|=
name|user
operator|.
name|getShortName
argument_list|()
operator|+
literal|"__"
operator|+
name|tblName
operator|+
literal|"__"
operator|+
operator|(
operator|new
name|BigInteger
argument_list|(
name|RANDOM_WIDTH
argument_list|,
name|random
argument_list|)
operator|.
name|toString
argument_list|(
name|RANDOM_RADIX
argument_list|)
operator|)
decl_stmt|;
return|return
name|createStagingDir
argument_list|(
name|baseDir
argument_list|,
name|user
argument_list|,
name|randomDir
argument_list|)
return|;
block|}
specifier|private
name|Path
name|createStagingDir
parameter_list|(
name|Path
name|baseDir
parameter_list|,
name|User
name|user
parameter_list|,
name|String
name|randomDir
parameter_list|)
throws|throws
name|IOException
block|{
name|Path
name|p
init|=
operator|new
name|Path
argument_list|(
name|baseDir
argument_list|,
name|randomDir
argument_list|)
decl_stmt|;
name|fs
operator|.
name|mkdirs
argument_list|(
name|p
argument_list|,
name|PERM_ALL_ACCESS
argument_list|)
expr_stmt|;
name|fs
operator|.
name|setPermission
argument_list|(
name|p
argument_list|,
name|PERM_ALL_ACCESS
argument_list|)
expr_stmt|;
return|return
name|p
return|;
block|}
specifier|private
name|User
name|getActiveUser
parameter_list|()
block|{
name|User
name|user
init|=
name|RequestContext
operator|.
name|getRequestUser
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|RequestContext
operator|.
name|isInRequestContext
argument_list|()
condition|)
block|{
return|return
literal|null
return|;
block|}
comment|//this is for testing
if|if
condition|(
literal|"simple"
operator|.
name|equalsIgnoreCase
argument_list|(
name|conf
operator|.
name|get
argument_list|(
name|User
operator|.
name|HBASE_SECURITY_CONF_KEY
argument_list|)
argument_list|)
condition|)
block|{
return|return
name|User
operator|.
name|createUserForTesting
argument_list|(
name|conf
argument_list|,
name|user
operator|.
name|getShortName
argument_list|()
argument_list|,
operator|new
name|String
index|[]
block|{}
argument_list|)
return|;
block|}
return|return
name|user
return|;
block|}
annotation|@
name|Override
specifier|public
name|Service
name|getService
parameter_list|()
block|{
return|return
name|this
return|;
block|}
specifier|private
specifier|static
class|class
name|SecureBulkLoadListener
implements|implements
name|HRegion
operator|.
name|BulkLoadListener
block|{
comment|// Target filesystem
specifier|private
name|FileSystem
name|fs
decl_stmt|;
specifier|private
name|String
name|stagingDir
decl_stmt|;
specifier|private
name|Configuration
name|conf
decl_stmt|;
comment|// Source filesystem
specifier|private
name|FileSystem
name|srcFs
init|=
literal|null
decl_stmt|;
specifier|public
name|SecureBulkLoadListener
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|String
name|stagingDir
parameter_list|,
name|Configuration
name|conf
parameter_list|)
block|{
name|this
operator|.
name|fs
operator|=
name|fs
expr_stmt|;
name|this
operator|.
name|stagingDir
operator|=
name|stagingDir
expr_stmt|;
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
name|String
name|prepareBulkLoad
parameter_list|(
specifier|final
name|byte
index|[]
name|family
parameter_list|,
specifier|final
name|String
name|srcPath
parameter_list|)
throws|throws
name|IOException
block|{
name|Path
name|p
init|=
operator|new
name|Path
argument_list|(
name|srcPath
argument_list|)
decl_stmt|;
name|Path
name|stageP
init|=
operator|new
name|Path
argument_list|(
name|stagingDir
argument_list|,
operator|new
name|Path
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|family
argument_list|)
argument_list|,
name|p
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|srcFs
operator|==
literal|null
condition|)
block|{
name|srcFs
operator|=
name|FileSystem
operator|.
name|get
argument_list|(
name|p
operator|.
name|toUri
argument_list|()
argument_list|,
name|conf
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|isFile
argument_list|(
name|p
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Path does not reference a file: "
operator|+
name|p
argument_list|)
throw|;
block|}
comment|// Check to see if the source and target filesystems are the same
if|if
condition|(
operator|!
name|FSHDFSUtils
operator|.
name|isSameHdfs
argument_list|(
name|conf
argument_list|,
name|srcFs
argument_list|,
name|fs
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Bulk-load file "
operator|+
name|srcPath
operator|+
literal|" is on different filesystem than "
operator|+
literal|"the destination filesystem. Copying file over to destination staging dir."
argument_list|)
expr_stmt|;
name|FileUtil
operator|.
name|copy
argument_list|(
name|srcFs
argument_list|,
name|p
argument_list|,
name|fs
argument_list|,
name|stageP
argument_list|,
literal|false
argument_list|,
name|conf
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Moving "
operator|+
name|p
operator|+
literal|" to "
operator|+
name|stageP
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|fs
operator|.
name|rename
argument_list|(
name|p
argument_list|,
name|stageP
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Failed to move HFile: "
operator|+
name|p
operator|+
literal|" to "
operator|+
name|stageP
argument_list|)
throw|;
block|}
block|}
return|return
name|stageP
operator|.
name|toString
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|doneBulkLoad
parameter_list|(
name|byte
index|[]
name|family
parameter_list|,
name|String
name|srcPath
parameter_list|)
throws|throws
name|IOException
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Bulk Load done for: "
operator|+
name|srcPath
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|failedBulkLoad
parameter_list|(
specifier|final
name|byte
index|[]
name|family
parameter_list|,
specifier|final
name|String
name|srcPath
parameter_list|)
throws|throws
name|IOException
block|{
name|Path
name|p
init|=
operator|new
name|Path
argument_list|(
name|srcPath
argument_list|)
decl_stmt|;
name|Path
name|stageP
init|=
operator|new
name|Path
argument_list|(
name|stagingDir
argument_list|,
operator|new
name|Path
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|family
argument_list|)
argument_list|,
name|p
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Moving "
operator|+
name|stageP
operator|+
literal|" back to "
operator|+
name|p
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|fs
operator|.
name|rename
argument_list|(
name|stageP
argument_list|,
name|p
argument_list|)
condition|)
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Failed to move HFile: "
operator|+
name|stageP
operator|+
literal|" to "
operator|+
name|p
argument_list|)
throw|;
block|}
comment|/**      * Check if the path is referencing a file.      * This is mainly needed to avoid symlinks.      * @param p      * @return true if the p is a file      * @throws IOException      */
specifier|private
name|boolean
name|isFile
parameter_list|(
name|Path
name|p
parameter_list|)
throws|throws
name|IOException
block|{
name|FileStatus
name|status
init|=
name|srcFs
operator|.
name|getFileStatus
argument_list|(
name|p
argument_list|)
decl_stmt|;
name|boolean
name|isFile
init|=
operator|!
name|status
operator|.
name|isDirectory
argument_list|()
decl_stmt|;
try|try
block|{
name|isFile
operator|=
name|isFile
operator|&&
operator|!
operator|(
name|Boolean
operator|)
name|Methods
operator|.
name|call
argument_list|(
name|FileStatus
operator|.
name|class
argument_list|,
name|status
argument_list|,
literal|"isSymlink"
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{       }
return|return
name|isFile
return|;
block|}
block|}
block|}
end_class

end_unit

