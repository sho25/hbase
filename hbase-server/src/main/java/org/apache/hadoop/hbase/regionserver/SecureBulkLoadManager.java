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
name|regionserver
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
name|HashMap
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
name|ipc
operator|.
name|RpcServer
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
name|Region
operator|.
name|BulkLoadListener
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
name|security
operator|.
name|token
operator|.
name|TokenUtil
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
name|FSUtils
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
name|shaded
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
name|PrepareBulkLoadRequest
import|;
end_import

begin_comment
comment|/**  * Bulk loads in secure mode.  *  * This service addresses two issues:  *<ol>  *<li>Moving files in a secure filesystem wherein the HBase Client  * and HBase Server are different filesystem users.</li>  *<li>Does moving in a secure manner. Assuming that the filesystem  * is POSIX compliant.</li>  *</ol>  *  * The algorithm is as follows:  *<ol>  *<li>Create an hbase owned staging directory which is  * world traversable (711): {@code /hbase/staging}</li>  *<li>A user writes out data to his secure output directory: {@code /user/foo/data}</li>  *<li>A call is made to hbase to create a secret staging directory  * which globally rwx (777): {@code /user/staging/averylongandrandomdirectoryname}</li>  *<li>The user moves the data into the random staging directory,  * then calls bulkLoadHFiles()</li>  *</ol>  *  * Like delegation tokens the strength of the security lies in the length  * and randomness of the secret directory.  *  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|SecureBulkLoadManager
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
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|SecureBulkLoadManager
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
name|UserProvider
name|userProvider
decl_stmt|;
specifier|private
name|Connection
name|conn
decl_stmt|;
name|SecureBulkLoadManager
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|Connection
name|conn
parameter_list|)
block|{
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|this
operator|.
name|conn
operator|=
name|conn
expr_stmt|;
block|}
specifier|public
name|void
name|start
parameter_list|()
throws|throws
name|IOException
block|{
name|random
operator|=
operator|new
name|SecureRandom
argument_list|()
expr_stmt|;
name|userProvider
operator|=
name|UserProvider
operator|.
name|instantiate
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|fs
operator|=
name|FileSystem
operator|.
name|get
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|baseStagingDir
operator|=
operator|new
name|Path
argument_list|(
name|FSUtils
operator|.
name|getRootDir
argument_list|(
name|conf
argument_list|)
argument_list|,
name|HConstants
operator|.
name|BULKLOAD_STAGING_DIR_NAME
argument_list|)
expr_stmt|;
if|if
condition|(
name|conf
operator|.
name|get
argument_list|(
literal|"hbase.bulkload.staging.dir"
argument_list|)
operator|!=
literal|null
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"hbase.bulkload.staging.dir "
operator|+
literal|" is deprecated. Bulkload staging directory is "
operator|+
name|baseStagingDir
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|fs
operator|.
name|exists
argument_list|(
name|baseStagingDir
argument_list|)
condition|)
block|{
name|fs
operator|.
name|mkdirs
argument_list|(
name|baseStagingDir
argument_list|,
name|PERM_HIDDEN
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|stop
parameter_list|()
throws|throws
name|IOException
block|{   }
specifier|public
name|String
name|prepareBulkLoad
parameter_list|(
specifier|final
name|Region
name|region
parameter_list|,
specifier|final
name|PrepareBulkLoadRequest
name|request
parameter_list|)
throws|throws
name|IOException
block|{
name|User
name|user
init|=
name|getActiveUser
argument_list|()
decl_stmt|;
name|region
operator|.
name|getCoprocessorHost
argument_list|()
operator|.
name|prePrepareBulkLoad
argument_list|(
name|user
argument_list|)
expr_stmt|;
name|String
name|bulkToken
init|=
name|createStagingDir
argument_list|(
name|baseStagingDir
argument_list|,
name|user
argument_list|,
name|region
operator|.
name|getTableDescriptor
argument_list|()
operator|.
name|getTableName
argument_list|()
argument_list|)
operator|.
name|toString
argument_list|()
decl_stmt|;
return|return
name|bulkToken
return|;
block|}
specifier|public
name|void
name|cleanupBulkLoad
parameter_list|(
specifier|final
name|Region
name|region
parameter_list|,
specifier|final
name|CleanupBulkLoadRequest
name|request
parameter_list|)
throws|throws
name|IOException
block|{
name|region
operator|.
name|getCoprocessorHost
argument_list|()
operator|.
name|preCleanupBulkLoad
argument_list|(
name|getActiveUser
argument_list|()
argument_list|)
expr_stmt|;
name|Path
name|path
init|=
operator|new
name|Path
argument_list|(
name|request
operator|.
name|getBulkToken
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|fs
operator|.
name|delete
argument_list|(
name|path
argument_list|,
literal|true
argument_list|)
condition|)
block|{
if|if
condition|(
name|fs
operator|.
name|exists
argument_list|(
name|path
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Failed to clean up "
operator|+
name|path
argument_list|)
throw|;
block|}
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Cleaned up "
operator|+
name|path
operator|+
literal|" successfully."
argument_list|)
expr_stmt|;
block|}
specifier|public
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
name|secureBulkLoadHFiles
parameter_list|(
specifier|final
name|Region
name|region
parameter_list|,
specifier|final
name|BulkLoadHFileRequest
name|request
parameter_list|)
throws|throws
name|IOException
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
argument_list|<>
argument_list|(
name|request
operator|.
name|getFamilyPathCount
argument_list|()
argument_list|)
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
argument_list|<>
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
name|Token
name|userToken
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|userProvider
operator|.
name|isHadoopSecurityEnabled
argument_list|()
condition|)
block|{
name|userToken
operator|=
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
expr_stmt|;
block|}
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
name|userProvider
operator|.
name|isHadoopSecurityEnabled
argument_list|()
condition|)
block|{
try|try
block|{
name|Token
name|tok
init|=
name|TokenUtil
operator|.
name|obtainToken
argument_list|(
name|conn
argument_list|)
decl_stmt|;
if|if
condition|(
name|tok
operator|!=
literal|null
condition|)
block|{
name|boolean
name|b
init|=
name|ugi
operator|.
name|addToken
argument_list|(
name|tok
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"token added "
operator|+
name|tok
operator|+
literal|" for user "
operator|+
name|ugi
operator|+
literal|" return="
operator|+
name|b
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|ioe
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"unable to add token"
argument_list|,
name|ioe
argument_list|)
expr_stmt|;
block|}
block|}
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
throw|throw
operator|new
name|DoNotRetryIOException
argument_list|(
literal|"User token cannot be null"
argument_list|)
throw|;
block|}
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
if|if
condition|(
name|userProvider
operator|.
name|isHadoopSecurityEnabled
argument_list|()
condition|)
block|{
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
name|targetfsDelegationToken
operator|.
name|acquireDelegationToken
argument_list|(
name|fs
argument_list|)
expr_stmt|;
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
block|}
name|map
operator|=
name|ugi
operator|.
name|doAs
argument_list|(
operator|new
name|PrivilegedAction
argument_list|<
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
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
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
name|region
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
argument_list|,
name|request
operator|.
name|getCopyFile
argument_list|()
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
literal|null
return|;
block|}
block|}
argument_list|)
expr_stmt|;
if|if
condition|(
name|map
operator|!=
literal|null
condition|)
block|{
name|loaded
operator|=
literal|true
expr_stmt|;
block|}
block|}
block|}
finally|finally
block|{
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
name|region
operator|.
name|getCoprocessorHost
argument_list|()
operator|.
name|postBulkLoadHFile
argument_list|(
name|familyPaths
argument_list|,
name|map
argument_list|,
name|loaded
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|map
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
throws|throws
name|IOException
block|{
comment|// for non-rpc handling, fallback to system user
name|User
name|user
init|=
name|RpcServer
operator|.
name|getRequestUser
argument_list|()
operator|.
name|orElse
argument_list|(
name|userProvider
operator|.
name|getCurrent
argument_list|()
argument_list|)
decl_stmt|;
comment|// this is for testing
if|if
condition|(
name|userProvider
operator|.
name|isHadoopSecurityEnabled
argument_list|()
operator|&&
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
specifier|private
specifier|static
class|class
name|SecureBulkLoadListener
implements|implements
name|BulkLoadListener
block|{
comment|// Target filesystem
specifier|private
specifier|final
name|FileSystem
name|fs
decl_stmt|;
specifier|private
specifier|final
name|String
name|stagingDir
decl_stmt|;
specifier|private
specifier|final
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
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|FsPermission
argument_list|>
name|origPermissions
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
name|this
operator|.
name|origPermissions
operator|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
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
parameter_list|,
name|boolean
name|copyFile
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
comment|// In case of Replication for bulk load files, hfiles are already copied in staging directory
if|if
condition|(
name|p
operator|.
name|equals
argument_list|(
name|stageP
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
name|p
operator|.
name|getName
argument_list|()
operator|+
literal|" is already available in staging directory. Skipping copy or rename."
argument_list|)
expr_stmt|;
return|return
name|stageP
operator|.
name|toString
argument_list|()
return|;
block|}
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
elseif|else
if|if
condition|(
name|copyFile
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
literal|" is copied to destination staging dir."
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
name|FileStatus
name|origFileStatus
init|=
name|fs
operator|.
name|getFileStatus
argument_list|(
name|p
argument_list|)
decl_stmt|;
name|origPermissions
operator|.
name|put
argument_list|(
name|srcPath
argument_list|,
name|origFileStatus
operator|.
name|getPermission
argument_list|()
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
name|fs
operator|.
name|setPermission
argument_list|(
name|stageP
argument_list|,
name|PERM_ALL_ACCESS
argument_list|)
expr_stmt|;
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
comment|// files are copied so no need to move them back
return|return;
block|}
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
comment|// In case of Replication for bulk load files, hfiles are not renamed by end point during
comment|// prepare stage, so no need of rename here again
if|if
condition|(
name|p
operator|.
name|equals
argument_list|(
name|stageP
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
name|p
operator|.
name|getName
argument_list|()
operator|+
literal|" is already available in source directory. Skipping rename."
argument_list|)
expr_stmt|;
return|return;
block|}
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
comment|// restore original permission
if|if
condition|(
name|origPermissions
operator|.
name|containsKey
argument_list|(
name|srcPath
argument_list|)
condition|)
block|{
name|fs
operator|.
name|setPermission
argument_list|(
name|p
argument_list|,
name|origPermissions
operator|.
name|get
argument_list|(
name|srcPath
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Can't find previous permission for path="
operator|+
name|srcPath
argument_list|)
expr_stmt|;
block|}
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

