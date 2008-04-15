begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2007 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|DataInputStream
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
name|net
operator|.
name|URI
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|URISyntaxException
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
name|dfs
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
name|FSDataInputStream
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
name|FileSystemVersionException
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
name|RemoteExceptionHandler
import|;
end_import

begin_comment
comment|/**  * Utility methods for interacting with the underlying file system.  */
end_comment

begin_class
specifier|public
class|class
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
name|FSUtils
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/**    * Not instantiable    */
specifier|private
name|FSUtils
parameter_list|()
block|{}
comment|/**    * Checks to see if the specified file system is available    *     * @param fs    * @throws IOException    */
specifier|public
specifier|static
name|void
name|checkFileSystemAvailable
parameter_list|(
specifier|final
name|FileSystem
name|fs
parameter_list|)
throws|throws
name|IOException
block|{
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
name|IOException
name|exception
init|=
literal|null
decl_stmt|;
name|DistributedFileSystem
name|dfs
init|=
operator|(
name|DistributedFileSystem
operator|)
name|fs
decl_stmt|;
try|try
block|{
if|if
condition|(
name|dfs
operator|.
name|exists
argument_list|(
operator|new
name|Path
argument_list|(
literal|"/"
argument_list|)
argument_list|)
condition|)
block|{
return|return;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|exception
operator|=
name|RemoteExceptionHandler
operator|.
name|checkIOException
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|fs
operator|.
name|close
argument_list|()
expr_stmt|;
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
literal|"file system close failed: "
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
name|IOException
name|io
init|=
operator|new
name|IOException
argument_list|(
literal|"File system is not available"
argument_list|)
decl_stmt|;
name|io
operator|.
name|initCause
argument_list|(
name|exception
argument_list|)
expr_stmt|;
throw|throw
name|io
throw|;
block|}
comment|/**    * Verifies current version of file system    *     * @param fs    * @param rootdir    * @return null if no version file exists, version string otherwise.    * @throws IOException    */
specifier|public
specifier|static
name|String
name|getVersion
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|Path
name|rootdir
parameter_list|)
throws|throws
name|IOException
block|{
name|Path
name|versionFile
init|=
operator|new
name|Path
argument_list|(
name|rootdir
argument_list|,
name|HConstants
operator|.
name|VERSION_FILE_NAME
argument_list|)
decl_stmt|;
name|String
name|version
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|fs
operator|.
name|exists
argument_list|(
name|versionFile
argument_list|)
condition|)
block|{
name|FSDataInputStream
name|s
init|=
name|fs
operator|.
name|open
argument_list|(
operator|new
name|Path
argument_list|(
name|rootdir
argument_list|,
name|HConstants
operator|.
name|VERSION_FILE_NAME
argument_list|)
argument_list|)
decl_stmt|;
try|try
block|{
name|version
operator|=
name|DataInputStream
operator|.
name|readUTF
argument_list|(
name|s
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|s
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
return|return
name|version
return|;
block|}
comment|/**    * Verifies current version of file system    *     * @param fs file system    * @param rootdir root directory of HBase installation    * @param message if true, issues a message on System.out     *     * @throws IOException    */
specifier|public
specifier|static
name|void
name|checkVersion
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|Path
name|rootdir
parameter_list|,
name|boolean
name|message
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|version
init|=
name|getVersion
argument_list|(
name|fs
argument_list|,
name|rootdir
argument_list|)
decl_stmt|;
if|if
condition|(
name|version
operator|==
literal|null
operator|||
name|version
operator|.
name|compareTo
argument_list|(
name|HConstants
operator|.
name|FILE_SYSTEM_VERSION
argument_list|)
operator|!=
literal|0
condition|)
block|{
comment|// Output on stdout so user sees it in terminal.
name|String
name|msg
init|=
literal|"File system needs to be upgraded. Run "
operator|+
literal|"the '${HBASE_HOME}/bin/hbase migrate' script."
decl_stmt|;
if|if
condition|(
name|message
condition|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"WARNING! "
operator|+
name|msg
argument_list|)
expr_stmt|;
block|}
throw|throw
operator|new
name|FileSystemVersionException
argument_list|(
name|msg
argument_list|)
throw|;
block|}
block|}
comment|/**    * Sets version of file system    *     * @param fs    * @param rootdir    * @throws IOException    */
specifier|public
specifier|static
name|void
name|setVersion
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|Path
name|rootdir
parameter_list|)
throws|throws
name|IOException
block|{
name|FSDataOutputStream
name|s
init|=
name|fs
operator|.
name|create
argument_list|(
operator|new
name|Path
argument_list|(
name|rootdir
argument_list|,
name|HConstants
operator|.
name|VERSION_FILE_NAME
argument_list|)
argument_list|)
decl_stmt|;
name|s
operator|.
name|writeUTF
argument_list|(
name|HConstants
operator|.
name|FILE_SYSTEM_VERSION
argument_list|)
expr_stmt|;
name|s
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
comment|/**    * Verifies root directory path is a valid URI with a scheme    *     * @param root root directory path    * @throws IOException if not a valid URI with a scheme    */
specifier|public
specifier|static
name|void
name|validateRootPath
parameter_list|(
name|Path
name|root
parameter_list|)
throws|throws
name|IOException
block|{
try|try
block|{
name|URI
name|rootURI
init|=
operator|new
name|URI
argument_list|(
name|root
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
name|String
name|scheme
init|=
name|rootURI
operator|.
name|getScheme
argument_list|()
decl_stmt|;
if|if
condition|(
name|scheme
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Root directory does not contain a scheme"
argument_list|)
throw|;
block|}
block|}
catch|catch
parameter_list|(
name|URISyntaxException
name|e
parameter_list|)
block|{
name|IOException
name|io
init|=
operator|new
name|IOException
argument_list|(
literal|"Root directory path is not a valid URI"
argument_list|)
decl_stmt|;
name|io
operator|.
name|initCause
argument_list|(
name|e
argument_list|)
expr_stmt|;
throw|throw
name|io
throw|;
block|}
block|}
block|}
end_class

end_unit

