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
name|server
operator|.
name|namenode
operator|.
name|LeaseExpiredException
import|;
end_import

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

begin_comment
comment|/**  * Implementation for hdfs  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
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
comment|/**    * Recover the lease from HDFS, retrying multiple times.    */
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
comment|// lease recovery not needed for local file system case.
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
name|DistributedFileSystem
name|dfs
init|=
operator|(
name|DistributedFileSystem
operator|)
name|fs
decl_stmt|;
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
name|EnvironmentEdgeManager
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
comment|// Default is 15 minutes. It's huge, but the idea is that if we have a major issue, HDFS
comment|//  usually needs 10 minutes before marking the nodes as dead. So we're putting ourselves
comment|//  beyond that limit 'to be safe'.
name|long
name|recoveryTimeout
init|=
name|conf
operator|.
name|getInt
argument_list|(
literal|"hbase.lease.recovery.timeout"
argument_list|,
literal|900000
argument_list|)
operator|+
name|startWaiting
decl_stmt|;
name|boolean
name|recovered
init|=
literal|false
decl_stmt|;
name|int
name|nbAttempt
init|=
literal|0
decl_stmt|;
while|while
condition|(
operator|!
name|recovered
condition|)
block|{
name|nbAttempt
operator|++
expr_stmt|;
try|try
block|{
comment|// recoverLease is asynchronous. We expect it to return true at the first call if the
comment|//  file is closed. So, it returns:
comment|//    - false when it starts the lease recovery (i.e. lease recovery not *yet* done
comment|//    - true when the lease recovery has succeeded or the file is closed.
name|recovered
operator|=
name|dfs
operator|.
name|recoverLease
argument_list|(
name|p
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Attempt "
operator|+
name|nbAttempt
operator|+
literal|" to recoverLease on file "
operator|+
name|p
operator|+
literal|" returned "
operator|+
name|recovered
operator|+
literal|", trying for "
operator|+
operator|(
name|EnvironmentEdgeManager
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|startWaiting
operator|)
operator|+
literal|"ms"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
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
argument_list|)
throw|;
block|}
name|LOG
operator|.
name|warn
argument_list|(
literal|"Got IOException on attempt "
operator|+
name|nbAttempt
operator|+
literal|" to recover lease for file "
operator|+
name|p
operator|+
literal|", retrying."
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|recovered
condition|)
block|{
comment|// try at least twice.
if|if
condition|(
name|nbAttempt
operator|>
literal|2
operator|&&
name|recoveryTimeout
operator|<
name|EnvironmentEdgeManager
operator|.
name|currentTimeMillis
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Can't recoverLease after "
operator|+
name|nbAttempt
operator|+
literal|" attempts and "
operator|+
operator|(
name|EnvironmentEdgeManager
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|startWaiting
operator|)
operator|+
literal|"ms "
operator|+
literal|" for "
operator|+
name|p
operator|+
literal|" - continuing without the lease, but we could have a data loss."
argument_list|)
expr_stmt|;
block|}
else|else
block|{
try|try
block|{
name|Thread
operator|.
name|sleep
argument_list|(
name|nbAttempt
operator|<
literal|3
condition|?
literal|500
else|:
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
block|}
block|}
block|}
block|}
end_class

end_unit

