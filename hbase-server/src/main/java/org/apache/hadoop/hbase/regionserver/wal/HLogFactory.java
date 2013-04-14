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
name|regionserver
operator|.
name|wal
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
name|io
operator|.
name|InterruptedIOException
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
name|regionserver
operator|.
name|wal
operator|.
name|HLog
operator|.
name|Reader
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
name|wal
operator|.
name|HLog
operator|.
name|Writer
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
name|EnvironmentEdgeManager
import|;
end_import

begin_class
specifier|public
class|class
name|HLogFactory
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
name|HLogFactory
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
specifier|static
name|HLog
name|createHLog
parameter_list|(
specifier|final
name|FileSystem
name|fs
parameter_list|,
specifier|final
name|Path
name|root
parameter_list|,
specifier|final
name|String
name|logName
parameter_list|,
specifier|final
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|FSHLog
argument_list|(
name|fs
argument_list|,
name|root
argument_list|,
name|logName
argument_list|,
name|conf
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|HLog
name|createHLog
parameter_list|(
specifier|final
name|FileSystem
name|fs
parameter_list|,
specifier|final
name|Path
name|root
parameter_list|,
specifier|final
name|String
name|logName
parameter_list|,
specifier|final
name|String
name|oldLogName
parameter_list|,
specifier|final
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|FSHLog
argument_list|(
name|fs
argument_list|,
name|root
argument_list|,
name|logName
argument_list|,
name|oldLogName
argument_list|,
name|conf
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|HLog
name|createHLog
parameter_list|(
specifier|final
name|FileSystem
name|fs
parameter_list|,
specifier|final
name|Path
name|root
parameter_list|,
specifier|final
name|String
name|logName
parameter_list|,
specifier|final
name|Configuration
name|conf
parameter_list|,
specifier|final
name|List
argument_list|<
name|WALActionsListener
argument_list|>
name|listeners
parameter_list|,
specifier|final
name|String
name|prefix
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|FSHLog
argument_list|(
name|fs
argument_list|,
name|root
argument_list|,
name|logName
argument_list|,
name|conf
argument_list|,
name|listeners
argument_list|,
name|prefix
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|HLog
name|createMetaHLog
parameter_list|(
specifier|final
name|FileSystem
name|fs
parameter_list|,
specifier|final
name|Path
name|root
parameter_list|,
specifier|final
name|String
name|logName
parameter_list|,
specifier|final
name|Configuration
name|conf
parameter_list|,
specifier|final
name|List
argument_list|<
name|WALActionsListener
argument_list|>
name|listeners
parameter_list|,
specifier|final
name|String
name|prefix
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|FSHLog
argument_list|(
name|fs
argument_list|,
name|root
argument_list|,
name|logName
argument_list|,
name|HConstants
operator|.
name|HREGION_OLDLOGDIR_NAME
argument_list|,
name|conf
argument_list|,
name|listeners
argument_list|,
literal|false
argument_list|,
name|prefix
argument_list|,
literal|true
argument_list|)
return|;
block|}
comment|/*      * WAL Reader      */
specifier|private
specifier|static
name|Class
argument_list|<
name|?
extends|extends
name|Reader
argument_list|>
name|logReaderClass
decl_stmt|;
specifier|static
name|void
name|resetLogReaderClass
parameter_list|()
block|{
name|logReaderClass
operator|=
literal|null
expr_stmt|;
block|}
comment|/**      * Create a reader for the WAL. If you are reading from a file that's being written to      * and need to reopen it multiple times, use {@link HLog.Reader#reset()} instead of this method      * then just seek back to the last known good position.      * @return A WAL reader.  Close when done with it.      * @throws IOException      */
specifier|public
specifier|static
name|HLog
operator|.
name|Reader
name|createReader
parameter_list|(
specifier|final
name|FileSystem
name|fs
parameter_list|,
specifier|final
name|Path
name|path
parameter_list|,
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|logReaderClass
operator|==
literal|null
condition|)
block|{
name|logReaderClass
operator|=
name|conf
operator|.
name|getClass
argument_list|(
literal|"hbase.regionserver.hlog.reader.impl"
argument_list|,
name|SequenceFileLogReader
operator|.
name|class
argument_list|,
name|Reader
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
try|try
block|{
comment|// A hlog file could be under recovery, so it may take several
comment|// tries to get it open. Instead of claiming it is corrupted, retry
comment|// to open it up to 5 minutes by default.
name|long
name|startWaiting
init|=
name|EnvironmentEdgeManager
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|long
name|openTimeout
init|=
name|conf
operator|.
name|getInt
argument_list|(
literal|"hbase.hlog.open.timeout"
argument_list|,
literal|300000
argument_list|)
operator|+
name|startWaiting
decl_stmt|;
name|int
name|nbAttempt
init|=
literal|0
decl_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
try|try
block|{
name|HLog
operator|.
name|Reader
name|reader
init|=
name|logReaderClass
operator|.
name|newInstance
argument_list|()
decl_stmt|;
name|reader
operator|.
name|init
argument_list|(
name|fs
argument_list|,
name|path
argument_list|,
name|conf
argument_list|)
expr_stmt|;
return|return
name|reader
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|String
name|msg
init|=
name|e
operator|.
name|getMessage
argument_list|()
decl_stmt|;
if|if
condition|(
name|msg
operator|!=
literal|null
operator|&&
name|msg
operator|.
name|contains
argument_list|(
literal|"Cannot obtain block length"
argument_list|)
condition|)
block|{
if|if
condition|(
operator|++
name|nbAttempt
operator|==
literal|1
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Lease should have recovered. This is not expected. Will retry"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|nbAttempt
operator|>
literal|2
operator|&&
name|openTimeout
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
literal|"Can't open after "
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
name|path
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
continue|continue;
comment|// retry
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
throw|throw
name|e
throw|;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|ie
parameter_list|)
block|{
throw|throw
name|ie
throw|;
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
literal|"Cannot get log reader"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
comment|/*      * WAL writer      */
specifier|private
specifier|static
name|Class
argument_list|<
name|?
extends|extends
name|Writer
argument_list|>
name|logWriterClass
decl_stmt|;
comment|/**      * Create a writer for the WAL.      * @return A WAL writer.  Close when done with it.      * @throws IOException      */
specifier|public
specifier|static
name|HLog
operator|.
name|Writer
name|createWriter
parameter_list|(
specifier|final
name|FileSystem
name|fs
parameter_list|,
specifier|final
name|Path
name|path
parameter_list|,
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
try|try
block|{
if|if
condition|(
name|logWriterClass
operator|==
literal|null
condition|)
block|{
name|logWriterClass
operator|=
name|conf
operator|.
name|getClass
argument_list|(
literal|"hbase.regionserver.hlog.writer.impl"
argument_list|,
name|SequenceFileLogWriter
operator|.
name|class
argument_list|,
name|Writer
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
name|HLog
operator|.
name|Writer
name|writer
init|=
operator|(
name|HLog
operator|.
name|Writer
operator|)
name|logWriterClass
operator|.
name|newInstance
argument_list|()
decl_stmt|;
name|writer
operator|.
name|init
argument_list|(
name|fs
argument_list|,
name|path
argument_list|,
name|conf
argument_list|)
expr_stmt|;
return|return
name|writer
return|;
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
literal|"cannot get log writer"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

