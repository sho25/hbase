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
name|master
operator|.
name|cleaner
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
name|LinkedList
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
name|Chore
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
name|hbase
operator|.
name|Stoppable
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

begin_comment
comment|/**  * Abstract Cleaner that uses a chain of delegates to clean a directory of files  * @param<T> Cleaner delegate class that is dynamically loaded from configuration  */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|CleanerChore
parameter_list|<
name|T
extends|extends
name|FileCleanerDelegate
parameter_list|>
extends|extends
name|Chore
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
name|CleanerChore
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|FileSystem
name|fs
decl_stmt|;
specifier|private
specifier|final
name|Path
name|oldFileDir
decl_stmt|;
specifier|private
specifier|final
name|Configuration
name|conf
decl_stmt|;
specifier|private
name|List
argument_list|<
name|T
argument_list|>
name|cleanersChain
decl_stmt|;
comment|/**    * @param name name of the chore being run    * @param sleepPeriod the period of time to sleep between each run    * @param s the stopper    * @param conf configuration to use    * @param fs handle to the FS    * @param oldFileDir the path to the archived files    * @param confKey configuration key for the classes to instantiate    */
specifier|public
name|CleanerChore
parameter_list|(
name|String
name|name
parameter_list|,
specifier|final
name|int
name|sleepPeriod
parameter_list|,
specifier|final
name|Stoppable
name|s
parameter_list|,
name|Configuration
name|conf
parameter_list|,
name|FileSystem
name|fs
parameter_list|,
name|Path
name|oldFileDir
parameter_list|,
name|String
name|confKey
parameter_list|)
block|{
name|super
argument_list|(
name|name
argument_list|,
name|sleepPeriod
argument_list|,
name|s
argument_list|)
expr_stmt|;
name|this
operator|.
name|fs
operator|=
name|fs
expr_stmt|;
name|this
operator|.
name|oldFileDir
operator|=
name|oldFileDir
expr_stmt|;
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|initCleanerChain
argument_list|(
name|confKey
argument_list|)
expr_stmt|;
block|}
comment|/**    * Validate the file to see if it even belongs in the directory. If it is valid, then the file    * will go through the cleaner delegates, but otherwise the file is just deleted.    * @param file full {@link Path} of the file to be checked    * @return<tt>true</tt> if the file is valid,<tt>false</tt> otherwise    */
specifier|protected
specifier|abstract
name|boolean
name|validate
parameter_list|(
name|Path
name|file
parameter_list|)
function_decl|;
comment|/**    * Instanitate and initialize all the file cleaners set in the configuration    * @param confKey key to get the file cleaner classes from the configuration    */
specifier|private
name|void
name|initCleanerChain
parameter_list|(
name|String
name|confKey
parameter_list|)
block|{
name|this
operator|.
name|cleanersChain
operator|=
operator|new
name|LinkedList
argument_list|<
name|T
argument_list|>
argument_list|()
expr_stmt|;
name|String
index|[]
name|logCleaners
init|=
name|conf
operator|.
name|getStrings
argument_list|(
name|confKey
argument_list|)
decl_stmt|;
if|if
condition|(
name|logCleaners
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|String
name|className
range|:
name|logCleaners
control|)
block|{
name|T
name|logCleaner
init|=
name|newFileCleaner
argument_list|(
name|className
argument_list|,
name|conf
argument_list|)
decl_stmt|;
if|if
condition|(
name|logCleaner
operator|!=
literal|null
condition|)
name|this
operator|.
name|cleanersChain
operator|.
name|add
argument_list|(
name|logCleaner
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**    * A utility method to create new instances of LogCleanerDelegate based on the class name of the    * LogCleanerDelegate.    * @param className fully qualified class name of the LogCleanerDelegate    * @param conf    * @return the new instance    */
specifier|public
name|T
name|newFileCleaner
parameter_list|(
name|String
name|className
parameter_list|,
name|Configuration
name|conf
parameter_list|)
block|{
try|try
block|{
name|Class
argument_list|<
name|?
extends|extends
name|FileCleanerDelegate
argument_list|>
name|c
init|=
name|Class
operator|.
name|forName
argument_list|(
name|className
argument_list|)
operator|.
name|asSubclass
argument_list|(
name|FileCleanerDelegate
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
name|T
name|cleaner
init|=
operator|(
name|T
operator|)
name|c
operator|.
name|newInstance
argument_list|()
decl_stmt|;
name|cleaner
operator|.
name|setConf
argument_list|(
name|conf
argument_list|)
expr_stmt|;
return|return
name|cleaner
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
name|warn
argument_list|(
literal|"Can NOT create CleanerDelegate: "
operator|+
name|className
argument_list|,
name|e
argument_list|)
expr_stmt|;
comment|// skipping if can't instantiate
return|return
literal|null
return|;
block|}
block|}
annotation|@
name|Override
specifier|protected
name|void
name|chore
parameter_list|()
block|{
try|try
block|{
name|FileStatus
index|[]
name|files
init|=
name|FSUtils
operator|.
name|listStatus
argument_list|(
name|this
operator|.
name|fs
argument_list|,
name|this
operator|.
name|oldFileDir
argument_list|,
literal|null
argument_list|)
decl_stmt|;
comment|// if the path (file or directory) doesn't exist, then we can just return
if|if
condition|(
name|files
operator|==
literal|null
condition|)
return|return;
comment|// loop over the found files and see if they should be deleted
for|for
control|(
name|FileStatus
name|file
range|:
name|files
control|)
block|{
try|try
block|{
if|if
condition|(
name|file
operator|.
name|isDir
argument_list|()
condition|)
name|checkDirectory
argument_list|(
name|file
operator|.
name|getPath
argument_list|()
argument_list|)
expr_stmt|;
else|else
name|checkAndDelete
argument_list|(
name|file
operator|.
name|getPath
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
name|e
operator|=
name|RemoteExceptionHandler
operator|.
name|checkIOException
argument_list|(
name|e
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|warn
argument_list|(
literal|"Error while cleaning the logs"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Failed to get status of:"
operator|+
name|oldFileDir
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Check to see if we can delete a directory (and all the children files of that directory).    *<p>    * A directory will not be deleted if it has children that are subsequently deleted since that    * will require another set of lookups in the filesystem, which is semantically same as waiting    * until the next time the chore is run, so we might as well wait.    * @param fs {@link FileSystem} where he directory resides    * @param toCheck directory to check    * @throws IOException    */
specifier|private
name|void
name|checkDirectory
parameter_list|(
name|Path
name|toCheck
parameter_list|)
throws|throws
name|IOException
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Checking directory: "
operator|+
name|toCheck
argument_list|)
expr_stmt|;
name|FileStatus
index|[]
name|files
init|=
name|checkAndDeleteDirectory
argument_list|(
name|toCheck
argument_list|)
decl_stmt|;
comment|// if the directory doesn't exist, then we are done
if|if
condition|(
name|files
operator|==
literal|null
condition|)
return|return;
comment|// otherwise we need to check each of the child files
for|for
control|(
name|FileStatus
name|file
range|:
name|files
control|)
block|{
name|Path
name|filePath
init|=
name|file
operator|.
name|getPath
argument_list|()
decl_stmt|;
comment|// if its a directory, then check to see if it should be deleted
if|if
condition|(
name|file
operator|.
name|isDir
argument_list|()
condition|)
block|{
comment|// check the subfiles to see if they can be deleted
name|checkDirectory
argument_list|(
name|filePath
argument_list|)
expr_stmt|;
continue|continue;
block|}
comment|// otherwise we can just check the file
name|checkAndDelete
argument_list|(
name|filePath
argument_list|)
expr_stmt|;
block|}
comment|// recheck the directory to see if we can delete it this time
name|checkAndDeleteDirectory
argument_list|(
name|toCheck
argument_list|)
expr_stmt|;
block|}
comment|/**    * Check and delete the passed directory if the directory is empty    * @param toCheck full path to the directory to check (and possibly delete)    * @return<tt>null</tt> if the directory was empty (and possibly deleted) and otherwise an array    *         of<code>FileStatus</code> for the files in the directory    * @throws IOException    */
specifier|private
name|FileStatus
index|[]
name|checkAndDeleteDirectory
parameter_list|(
name|Path
name|toCheck
parameter_list|)
throws|throws
name|IOException
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Attempting to delete directory:"
operator|+
name|toCheck
argument_list|)
expr_stmt|;
comment|// if it doesn't exist, we are done
if|if
condition|(
operator|!
name|fs
operator|.
name|exists
argument_list|(
name|toCheck
argument_list|)
condition|)
return|return
literal|null
return|;
comment|// get the files below the directory
name|FileStatus
index|[]
name|files
init|=
name|FSUtils
operator|.
name|listStatus
argument_list|(
name|fs
argument_list|,
name|toCheck
argument_list|,
literal|null
argument_list|)
decl_stmt|;
comment|// if there are no subfiles, then we can delete the directory
if|if
condition|(
name|files
operator|==
literal|null
condition|)
block|{
name|checkAndDelete
argument_list|(
name|toCheck
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
comment|// return the status of the files in the directory
return|return
name|files
return|;
block|}
comment|/**    * Run the given file through each of the cleaners to see if it should be deleted, deleting it if    * necessary.    * @param filePath path of the file to check (and possibly delete)    * @throws IOException if cann't delete a file because of a filesystem issue    * @throws IllegalArgumentException if the file is a directory and has children    */
specifier|private
name|void
name|checkAndDelete
parameter_list|(
name|Path
name|filePath
parameter_list|)
throws|throws
name|IOException
throws|,
name|IllegalArgumentException
block|{
if|if
condition|(
operator|!
name|validate
argument_list|(
name|filePath
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Found a wrongly formatted file: "
operator|+
name|filePath
operator|.
name|getName
argument_list|()
operator|+
literal|"deleting it."
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|this
operator|.
name|fs
operator|.
name|delete
argument_list|(
name|filePath
argument_list|,
literal|true
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Attempted to delete:"
operator|+
name|filePath
operator|+
literal|", but couldn't. Run cleaner chain and attempt to delete on next pass."
argument_list|)
expr_stmt|;
block|}
return|return;
block|}
for|for
control|(
name|T
name|cleaner
range|:
name|cleanersChain
control|)
block|{
if|if
condition|(
name|cleaner
operator|.
name|isStopped
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"A file cleaner"
operator|+
name|this
operator|.
name|getName
argument_list|()
operator|+
literal|" is stopped, won't delete any file in:"
operator|+
name|this
operator|.
name|oldFileDir
argument_list|)
expr_stmt|;
return|return;
block|}
if|if
condition|(
operator|!
name|cleaner
operator|.
name|isFileDeleteable
argument_list|(
name|filePath
argument_list|)
condition|)
block|{
comment|// this file is not deletable, then we are done
name|LOG
operator|.
name|debug
argument_list|(
name|filePath
operator|+
literal|" is not deletable according to:"
operator|+
name|cleaner
argument_list|)
expr_stmt|;
return|return;
block|}
block|}
comment|// delete this file if it passes all the cleaners
name|LOG
operator|.
name|debug
argument_list|(
literal|"Removing:"
operator|+
name|filePath
operator|+
literal|" from archive"
argument_list|)
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|fs
operator|.
name|delete
argument_list|(
name|filePath
argument_list|,
literal|false
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Attempted to delete:"
operator|+
name|filePath
operator|+
literal|", but couldn't. Run cleaner chain and attempt to delete on next pass."
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|cleanup
parameter_list|()
block|{
for|for
control|(
name|T
name|lc
range|:
name|this
operator|.
name|cleanersChain
control|)
block|{
try|try
block|{
name|lc
operator|.
name|stop
argument_list|(
literal|"Exiting"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Stopping"
argument_list|,
name|t
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

