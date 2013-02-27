begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|util
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|FileNotFoundException
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
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|InvocationTargetException
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
name|fs
operator|.
name|FSDataOutputStream
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
name|RemoteExceptionHandler
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
name|hdfs
operator|.
name|DistributedFileSystem
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
name|hdfs
operator|.
name|protocol
operator|.
name|AlreadyBeingCreatedException
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
name|hdfs
operator|.
name|server
operator|.
name|namenode
operator|.
name|LeaseExpiredException
import|;
end_import

begin_comment
comment|/**  * Implementation for hdfs  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
class|class
name|FSHDFSUtils
extends|extends
name|FSUtils
block|{
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
name|FSHDFSUtils
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/**    * Lease timeout constant, sourced from HDFS upstream.    * The upstream constant is defined in a private interface, so we    * can't reuse for compatibility reasons.    * NOTE: On versions earlier than Hadoop 0.23, the constant is in    * o.a.h.hdfs.protocol.FSConstants, while for 0.23 and above it is    * in o.a.h.hdfs.protocol.HdfsConstants cause of HDFS-1620.    */
specifier|public
specifier|static
specifier|final
name|long
name|LEASE_SOFTLIMIT_PERIOD
init|=
literal|60
operator|*
literal|1000
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|TEST_TRIGGER_DFS_APPEND
init|=
literal|"hbase.test.trigger.dfs.append"
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|recoverFileLease
parameter_list|(
specifier|final
name|FileSystem
name|fs
parameter_list|,
specifier|final
name|Path
name|p
parameter_list|,
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
operator|!
name|isAppendSupported
argument_list|(
name|conf
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Running on HDFS without append enabled may result in data loss"
argument_list|)
expr_stmt|;
return|return;
block|}
comment|// lease recovery not needed for local file system case.
comment|// currently, local file system doesn't implement append either.
if|if
condition|(
operator|!
operator|(
name|fs
operator|instanceof
name|DistributedFileSystem
operator|)
condition|)
block|{
return|return;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Recovering file "
operator|+
name|p
argument_list|)
expr_stmt|;
name|long
name|startWaiting
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
comment|// Trying recovery
name|boolean
name|recovered
init|=
literal|false
decl_stmt|;
name|long
name|recoveryTimeout
init|=
name|conf
operator|.
name|getInt
argument_list|(
literal|"hbase.lease.recovery.timeout"
argument_list|,
literal|300000
argument_list|)
decl_stmt|;
comment|// conf parameter passed from unit test, indicating whether fs.append() should be triggered
name|boolean
name|triggerAppend
init|=
name|conf
operator|.
name|getBoolean
argument_list|(
name|TEST_TRIGGER_DFS_APPEND
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|Exception
name|ex
init|=
literal|null
decl_stmt|;
while|while
condition|(
operator|!
name|recovered
condition|)
block|{
try|try
block|{
try|try
block|{
name|DistributedFileSystem
name|dfs
init|=
operator|(
name|DistributedFileSystem
operator|)
name|fs
decl_stmt|;
if|if
condition|(
name|triggerAppend
condition|)
throw|throw
operator|new
name|IOException
argument_list|()
throw|;
try|try
block|{
name|recovered
operator|=
operator|(
name|Boolean
operator|)
name|DistributedFileSystem
operator|.
name|class
operator|.
name|getMethod
argument_list|(
literal|"recoverLease"
argument_list|,
operator|new
name|Class
index|[]
block|{
name|Path
operator|.
name|class
block|}
argument_list|)
operator|.
name|invoke
argument_list|(
name|dfs
argument_list|,
name|p
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InvocationTargetException
name|ite
parameter_list|)
block|{
comment|// function was properly called, but threw it's own exception
throw|throw
operator|(
name|IOException
operator|)
name|ite
operator|.
name|getCause
argument_list|()
throw|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Failed fs.recoverLease invocation, "
operator|+
name|e
operator|.
name|toString
argument_list|()
operator|+
literal|", trying fs.append instead"
argument_list|)
expr_stmt|;
name|ex
operator|=
name|e
expr_stmt|;
block|}
if|if
condition|(
name|ex
operator|!=
literal|null
operator|||
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|startWaiting
operator|>
name|recoveryTimeout
condition|)
block|{
name|ex
operator|=
literal|null
expr_stmt|;
comment|// assume the following append() call would succeed
name|LOG
operator|.
name|debug
argument_list|(
literal|"trying fs.append for "
operator|+
name|p
argument_list|)
expr_stmt|;
name|FSDataOutputStream
name|out
init|=
name|fs
operator|.
name|append
argument_list|(
name|p
argument_list|)
decl_stmt|;
name|out
operator|.
name|close
argument_list|()
expr_stmt|;
name|recovered
operator|=
literal|true
expr_stmt|;
block|}
if|if
condition|(
name|recovered
condition|)
break|break;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|e
operator|=
name|RemoteExceptionHandler
operator|.
name|checkIOException
argument_list|(
name|e
argument_list|)
expr_stmt|;
if|if
condition|(
name|e
operator|instanceof
name|AlreadyBeingCreatedException
condition|)
block|{
comment|// We expect that we'll get this message while the lease is still
comment|// within its soft limit, but if we get it past that, it means
comment|// that the RS is holding onto the file even though it lost its
comment|// znode. We could potentially abort after some time here.
name|long
name|waitedFor
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|startWaiting
decl_stmt|;
if|if
condition|(
name|waitedFor
operator|>
name|LEASE_SOFTLIMIT_PERIOD
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Waited "
operator|+
name|waitedFor
operator|+
literal|"ms for lease recovery on "
operator|+
name|p
operator|+
literal|":"
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|e
operator|instanceof
name|LeaseExpiredException
operator|&&
name|e
operator|.
name|getMessage
argument_list|()
operator|.
name|contains
argument_list|(
literal|"File does not exist"
argument_list|)
condition|)
block|{
comment|// This exception comes out instead of FNFE, fix it
throw|throw
operator|new
name|FileNotFoundException
argument_list|(
literal|"The given HLog wasn't found at "
operator|+
name|p
operator|.
name|toString
argument_list|()
argument_list|)
throw|;
block|}
else|else
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Failed to open "
operator|+
name|p
operator|+
literal|" for append"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
try|try
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|1000
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|ie
parameter_list|)
block|{
name|InterruptedIOException
name|iioe
init|=
operator|new
name|InterruptedIOException
argument_list|()
decl_stmt|;
name|iioe
operator|.
name|initCause
argument_list|(
name|ie
argument_list|)
expr_stmt|;
throw|throw
name|iioe
throw|;
block|}
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Finished lease recover attempt for "
operator|+
name|p
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

