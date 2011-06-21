begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2010 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|OutputStream
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
name|Field
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
name|Method
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
name|io
operator|.
name|SequenceFile
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
name|SequenceFile
operator|.
name|Metadata
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
name|compress
operator|.
name|DefaultCodec
import|;
end_import

begin_comment
comment|/**  * Implementation of {@link HLog.Writer} that delegates to  * SequenceFile.Writer.  */
end_comment

begin_class
specifier|public
class|class
name|SequenceFileLogWriter
implements|implements
name|HLog
operator|.
name|Writer
block|{
specifier|private
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
argument_list|)
decl_stmt|;
comment|// The sequence file we delegate to.
specifier|private
name|SequenceFile
operator|.
name|Writer
name|writer
decl_stmt|;
comment|// This is the FSDataOutputStream instance that is the 'out' instance
comment|// in the SequenceFile.Writer 'writer' instance above.
specifier|private
name|FSDataOutputStream
name|writer_out
decl_stmt|;
specifier|private
name|Class
argument_list|<
name|?
extends|extends
name|HLogKey
argument_list|>
name|keyClass
decl_stmt|;
specifier|private
name|Method
name|syncFs
init|=
literal|null
decl_stmt|;
specifier|private
name|Method
name|hflush
init|=
literal|null
decl_stmt|;
comment|/**    * Default constructor.    */
specifier|public
name|SequenceFileLogWriter
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
comment|/**    * This constructor allows a specific HLogKey implementation to override that    * which would otherwise be chosen via configuration property.    *     * @param keyClass    */
specifier|public
name|SequenceFileLogWriter
parameter_list|(
name|Class
argument_list|<
name|?
extends|extends
name|HLogKey
argument_list|>
name|keyClass
parameter_list|)
block|{
name|this
operator|.
name|keyClass
operator|=
name|keyClass
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|init
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
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
literal|null
operator|==
name|keyClass
condition|)
block|{
name|keyClass
operator|=
name|HLog
operator|.
name|getKeyClass
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
comment|// Create a SF.Writer instance.
name|this
operator|.
name|writer
operator|=
name|SequenceFile
operator|.
name|createWriter
argument_list|(
name|fs
argument_list|,
name|conf
argument_list|,
name|path
argument_list|,
name|keyClass
argument_list|,
name|WALEdit
operator|.
name|class
argument_list|,
name|fs
operator|.
name|getConf
argument_list|()
operator|.
name|getInt
argument_list|(
literal|"io.file.buffer.size"
argument_list|,
literal|4096
argument_list|)
argument_list|,
operator|(
name|short
operator|)
name|conf
operator|.
name|getInt
argument_list|(
literal|"hbase.regionserver.hlog.replication"
argument_list|,
name|fs
operator|.
name|getDefaultReplication
argument_list|()
argument_list|)
argument_list|,
name|conf
operator|.
name|getLong
argument_list|(
literal|"hbase.regionserver.hlog.blocksize"
argument_list|,
name|fs
operator|.
name|getDefaultBlockSize
argument_list|()
argument_list|)
argument_list|,
name|SequenceFile
operator|.
name|CompressionType
operator|.
name|NONE
argument_list|,
operator|new
name|DefaultCodec
argument_list|()
argument_list|,
literal|null
argument_list|,
operator|new
name|Metadata
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|writer_out
operator|=
name|getSequenceFilePrivateFSDataOutputStreamAccessible
argument_list|()
expr_stmt|;
name|this
operator|.
name|syncFs
operator|=
name|getSyncFs
argument_list|()
expr_stmt|;
name|this
operator|.
name|hflush
operator|=
name|getHFlush
argument_list|()
expr_stmt|;
name|String
name|msg
init|=
literal|"syncFs="
operator|+
operator|(
name|this
operator|.
name|syncFs
operator|!=
literal|null
operator|)
operator|+
literal|", hflush="
operator|+
operator|(
name|this
operator|.
name|hflush
operator|!=
literal|null
operator|)
decl_stmt|;
if|if
condition|(
name|this
operator|.
name|syncFs
operator|!=
literal|null
operator|||
name|this
operator|.
name|hflush
operator|!=
literal|null
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
name|msg
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"No sync support! "
operator|+
name|msg
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Now do dirty work to see if syncFs is available on the backing this.writer.    * It will be available in branch-0.20-append and in CDH3.    * @return The syncFs method or null if not available.    * @throws IOException    */
specifier|private
name|Method
name|getSyncFs
parameter_list|()
throws|throws
name|IOException
block|{
name|Method
name|m
init|=
literal|null
decl_stmt|;
try|try
block|{
comment|// function pointer to writer.syncFs() method; present when sync is hdfs-200.
name|m
operator|=
name|this
operator|.
name|writer
operator|.
name|getClass
argument_list|()
operator|.
name|getMethod
argument_list|(
literal|"syncFs"
argument_list|,
operator|new
name|Class
argument_list|<
name|?
argument_list|>
index|[]
block|{}
block|)
empty_stmt|;
block|}
catch|catch
parameter_list|(
name|SecurityException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Failed test for syncfs"
argument_list|,
name|e
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|NoSuchMethodException
name|e
parameter_list|)
block|{
comment|// Not available
block|}
return|return
name|m
return|;
block|}
end_class

begin_comment
comment|/**    * See if hflush (0.21 and 0.22 hadoop) is available.    * @return The hflush method or null if not available.    * @throws IOException    */
end_comment

begin_function
specifier|private
name|Method
name|getHFlush
parameter_list|()
throws|throws
name|IOException
block|{
name|Method
name|m
init|=
literal|null
decl_stmt|;
try|try
block|{
name|Class
argument_list|<
name|?
extends|extends
name|OutputStream
argument_list|>
name|c
init|=
name|getWriterFSDataOutputStream
argument_list|()
operator|.
name|getClass
argument_list|()
decl_stmt|;
name|m
operator|=
name|c
operator|.
name|getMethod
argument_list|(
literal|"hflush"
argument_list|,
operator|new
name|Class
argument_list|<
name|?
argument_list|>
index|[]
block|{}
block|)
empty_stmt|;
block|}
end_function

begin_catch
catch|catch
parameter_list|(
name|SecurityException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Failed test for hflush"
argument_list|,
name|e
argument_list|)
throw|;
block|}
end_catch

begin_catch
catch|catch
parameter_list|(
name|NoSuchMethodException
name|e
parameter_list|)
block|{
comment|// Ignore
block|}
end_catch

begin_return
return|return
name|m
return|;
end_return

begin_comment
unit|}
comment|// Get at the private FSDataOutputStream inside in SequenceFile so we can
end_comment

begin_comment
comment|// call sync on it.  Make it accessible.
end_comment

begin_function
unit|private
name|FSDataOutputStream
name|getSequenceFilePrivateFSDataOutputStreamAccessible
parameter_list|()
throws|throws
name|IOException
block|{
name|FSDataOutputStream
name|out
init|=
literal|null
decl_stmt|;
specifier|final
name|Field
name|fields
index|[]
init|=
name|this
operator|.
name|writer
operator|.
name|getClass
argument_list|()
operator|.
name|getDeclaredFields
argument_list|()
decl_stmt|;
specifier|final
name|String
name|fieldName
init|=
literal|"out"
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
name|fields
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
if|if
condition|(
name|fieldName
operator|.
name|equals
argument_list|(
name|fields
index|[
name|i
index|]
operator|.
name|getName
argument_list|()
argument_list|)
condition|)
block|{
try|try
block|{
comment|// Make the 'out' field up in SF.Writer accessible.
name|fields
index|[
name|i
index|]
operator|.
name|setAccessible
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|out
operator|=
operator|(
name|FSDataOutputStream
operator|)
name|fields
index|[
name|i
index|]
operator|.
name|get
argument_list|(
name|this
operator|.
name|writer
argument_list|)
expr_stmt|;
break|break;
block|}
catch|catch
parameter_list|(
name|IllegalAccessException
name|ex
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Accessing "
operator|+
name|fieldName
argument_list|,
name|ex
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|SecurityException
name|e
parameter_list|)
block|{
comment|// TODO Auto-generated catch block
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
block|}
block|}
return|return
name|out
return|;
block|}
end_function

begin_function
annotation|@
name|Override
specifier|public
name|void
name|append
parameter_list|(
name|HLog
operator|.
name|Entry
name|entry
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|writer
operator|.
name|append
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|entry
operator|.
name|getEdit
argument_list|()
argument_list|)
expr_stmt|;
block|}
end_function

begin_function
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|this
operator|.
name|writer
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|writer
operator|.
name|close
argument_list|()
expr_stmt|;
name|this
operator|.
name|writer
operator|=
literal|null
expr_stmt|;
block|}
block|}
end_function

begin_function
annotation|@
name|Override
specifier|public
name|void
name|sync
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|this
operator|.
name|syncFs
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|this
operator|.
name|syncFs
operator|.
name|invoke
argument_list|(
name|this
operator|.
name|writer
argument_list|,
name|HLog
operator|.
name|NO_ARGS
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
literal|"Reflection"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
elseif|else
if|if
condition|(
name|this
operator|.
name|hflush
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|this
operator|.
name|hflush
operator|.
name|invoke
argument_list|(
name|getWriterFSDataOutputStream
argument_list|()
argument_list|,
name|HLog
operator|.
name|NO_ARGS
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
literal|"Reflection"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
block|}
end_function

begin_function
annotation|@
name|Override
specifier|public
name|long
name|getLength
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|this
operator|.
name|writer
operator|.
name|getLength
argument_list|()
return|;
block|}
end_function

begin_comment
comment|/**    * @return The dfsclient out stream up inside SF.Writer made accessible, or    * null if not available.    */
end_comment

begin_function
specifier|public
name|FSDataOutputStream
name|getWriterFSDataOutputStream
parameter_list|()
block|{
return|return
name|this
operator|.
name|writer_out
return|;
block|}
end_function

unit|}
end_unit

