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
name|IOException
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
name|dfs
operator|.
name|DistributedFileSystem
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
comment|/**    * Checks to see if the specified file system is available    *     * @param fs    * @return true if the specified file system is available.    */
specifier|public
specifier|static
name|boolean
name|isFileSystemAvailable
parameter_list|(
specifier|final
name|FileSystem
name|fs
parameter_list|)
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
return|return
literal|true
return|;
block|}
name|String
name|exception
init|=
literal|""
decl_stmt|;
name|boolean
name|available
init|=
literal|false
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
name|available
operator|=
literal|true
expr_stmt|;
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
name|e
operator|.
name|getMessage
argument_list|()
expr_stmt|;
block|}
try|try
block|{
if|if
condition|(
operator|!
name|available
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Failed file system available test. Thread: "
operator|+
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|getName
argument_list|()
operator|+
literal|": "
operator|+
name|exception
argument_list|)
expr_stmt|;
name|fs
operator|.
name|close
argument_list|()
expr_stmt|;
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
name|error
argument_list|(
literal|"file system close failed: "
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
return|return
name|available
return|;
block|}
block|}
end_class

end_unit

