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
name|util
operator|.
name|NavigableSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|TreeSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|regex
operator|.
name|Matcher
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|regex
operator|.
name|Pattern
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
name|fs
operator|.
name|PathFilter
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
name|HRegionInfo
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
name|HTableDescriptor
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
name|ServerName
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
name|WALProtos
operator|.
name|CompactionDescriptor
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
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|TextFormat
import|;
end_import

begin_class
specifier|public
class|class
name|HLogUtil
block|{
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|HLogUtil
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/**    * Pattern used to validate a HLog file name    */
specifier|private
specifier|static
specifier|final
name|Pattern
name|pattern
init|=
name|Pattern
operator|.
name|compile
argument_list|(
literal|".*\\.\\d*("
operator|+
name|HLog
operator|.
name|META_HLOG_FILE_EXTN
operator|+
literal|")*"
argument_list|)
decl_stmt|;
comment|/**    * @param filename    *          name of the file to validate    * @return<tt>true</tt> if the filename matches an HLog,<tt>false</tt>    *         otherwise    */
specifier|public
specifier|static
name|boolean
name|validateHLogFilename
parameter_list|(
name|String
name|filename
parameter_list|)
block|{
return|return
name|pattern
operator|.
name|matcher
argument_list|(
name|filename
argument_list|)
operator|.
name|matches
argument_list|()
return|;
block|}
comment|/**    * Construct the HLog directory name    *    * @param serverName    *          Server name formatted as described in {@link ServerName}    * @return the relative HLog directory name, e.g.    *<code>.logs/1.example.org,60030,12345</code> if    *<code>serverName</code> passed is    *<code>1.example.org,60030,12345</code>    */
specifier|public
specifier|static
name|String
name|getHLogDirectoryName
parameter_list|(
specifier|final
name|String
name|serverName
parameter_list|)
block|{
name|StringBuilder
name|dirName
init|=
operator|new
name|StringBuilder
argument_list|(
name|HConstants
operator|.
name|HREGION_LOGDIR_NAME
argument_list|)
decl_stmt|;
name|dirName
operator|.
name|append
argument_list|(
literal|"/"
argument_list|)
expr_stmt|;
name|dirName
operator|.
name|append
argument_list|(
name|serverName
argument_list|)
expr_stmt|;
return|return
name|dirName
operator|.
name|toString
argument_list|()
return|;
block|}
comment|/**    * @param regiondir    *          This regions directory in the filesystem.    * @return The directory that holds recovered edits files for the region    *<code>regiondir</code>    */
specifier|public
specifier|static
name|Path
name|getRegionDirRecoveredEditsDir
parameter_list|(
specifier|final
name|Path
name|regiondir
parameter_list|)
block|{
return|return
operator|new
name|Path
argument_list|(
name|regiondir
argument_list|,
name|HConstants
operator|.
name|RECOVERED_EDITS_DIR
argument_list|)
return|;
block|}
comment|/**    * Move aside a bad edits file.    *    * @param fs    * @param edits    *          Edits file to move aside.    * @return The name of the moved aside file.    * @throws IOException    */
specifier|public
specifier|static
name|Path
name|moveAsideBadEditsFile
parameter_list|(
specifier|final
name|FileSystem
name|fs
parameter_list|,
specifier|final
name|Path
name|edits
parameter_list|)
throws|throws
name|IOException
block|{
name|Path
name|moveAsideName
init|=
operator|new
name|Path
argument_list|(
name|edits
operator|.
name|getParent
argument_list|()
argument_list|,
name|edits
operator|.
name|getName
argument_list|()
operator|+
literal|"."
operator|+
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|fs
operator|.
name|rename
argument_list|(
name|edits
argument_list|,
name|moveAsideName
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Rename failed from "
operator|+
name|edits
operator|+
literal|" to "
operator|+
name|moveAsideName
argument_list|)
expr_stmt|;
block|}
return|return
name|moveAsideName
return|;
block|}
comment|/**    * @param path    *          - the path to analyze. Expected format, if it's in hlog directory:    *          / [base directory for hbase] / hbase / .logs / ServerName /    *          logfile    * @return null if it's not a log file. Returns the ServerName of the region    *         server that created this log file otherwise.    */
specifier|public
specifier|static
name|ServerName
name|getServerNameFromHLogDirectoryName
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|String
name|path
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|path
operator|==
literal|null
operator|||
name|path
operator|.
name|length
argument_list|()
operator|<=
name|HConstants
operator|.
name|HREGION_LOGDIR_NAME
operator|.
name|length
argument_list|()
condition|)
block|{
return|return
literal|null
return|;
block|}
if|if
condition|(
name|conf
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"parameter conf must be set"
argument_list|)
throw|;
block|}
specifier|final
name|String
name|rootDir
init|=
name|conf
operator|.
name|get
argument_list|(
name|HConstants
operator|.
name|HBASE_DIR
argument_list|)
decl_stmt|;
if|if
condition|(
name|rootDir
operator|==
literal|null
operator|||
name|rootDir
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
name|HConstants
operator|.
name|HBASE_DIR
operator|+
literal|" key not found in conf."
argument_list|)
throw|;
block|}
specifier|final
name|StringBuilder
name|startPathSB
init|=
operator|new
name|StringBuilder
argument_list|(
name|rootDir
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|rootDir
operator|.
name|endsWith
argument_list|(
literal|"/"
argument_list|)
condition|)
name|startPathSB
operator|.
name|append
argument_list|(
literal|'/'
argument_list|)
expr_stmt|;
name|startPathSB
operator|.
name|append
argument_list|(
name|HConstants
operator|.
name|HREGION_LOGDIR_NAME
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|HConstants
operator|.
name|HREGION_LOGDIR_NAME
operator|.
name|endsWith
argument_list|(
literal|"/"
argument_list|)
condition|)
name|startPathSB
operator|.
name|append
argument_list|(
literal|'/'
argument_list|)
expr_stmt|;
specifier|final
name|String
name|startPath
init|=
name|startPathSB
operator|.
name|toString
argument_list|()
decl_stmt|;
name|String
name|fullPath
decl_stmt|;
try|try
block|{
name|fullPath
operator|=
name|FileSystem
operator|.
name|get
argument_list|(
name|conf
argument_list|)
operator|.
name|makeQualified
argument_list|(
operator|new
name|Path
argument_list|(
name|path
argument_list|)
argument_list|)
operator|.
name|toString
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Call to makeQualified failed on "
operator|+
name|path
operator|+
literal|" "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
if|if
condition|(
operator|!
name|fullPath
operator|.
name|startsWith
argument_list|(
name|startPath
argument_list|)
condition|)
block|{
return|return
literal|null
return|;
block|}
specifier|final
name|String
name|serverNameAndFile
init|=
name|fullPath
operator|.
name|substring
argument_list|(
name|startPath
operator|.
name|length
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|serverNameAndFile
operator|.
name|indexOf
argument_list|(
literal|'/'
argument_list|)
operator|<
literal|"a,0,0"
operator|.
name|length
argument_list|()
condition|)
block|{
comment|// Either it's a file (not a directory) or it's not a ServerName format
return|return
literal|null
return|;
block|}
specifier|final
name|String
name|serverName
init|=
name|serverNameAndFile
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|serverNameAndFile
operator|.
name|indexOf
argument_list|(
literal|'/'
argument_list|)
operator|-
literal|1
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|ServerName
operator|.
name|isFullServerName
argument_list|(
name|serverName
argument_list|)
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
name|ServerName
operator|.
name|parseServerName
argument_list|(
name|serverName
argument_list|)
return|;
block|}
comment|/**    * This function returns region server name from a log file name which is in either format:    * hdfs://<name node>/hbase/.logs/<server name>-splitting/... or hdfs://<name    * node>/hbase/.logs/<server name>/...    * @param logFile    * @return null if the passed in logFile isn't a valid HLog file path    */
specifier|public
specifier|static
name|ServerName
name|getServerNameFromHLogDirectoryName
parameter_list|(
name|Path
name|logFile
parameter_list|)
block|{
name|Path
name|logDir
init|=
name|logFile
operator|.
name|getParent
argument_list|()
decl_stmt|;
name|String
name|logDirName
init|=
name|logDir
operator|.
name|getName
argument_list|()
decl_stmt|;
if|if
condition|(
name|logDirName
operator|.
name|equals
argument_list|(
name|HConstants
operator|.
name|HREGION_LOGDIR_NAME
argument_list|)
condition|)
block|{
name|logDir
operator|=
name|logFile
expr_stmt|;
name|logDirName
operator|=
name|logDir
operator|.
name|getName
argument_list|()
expr_stmt|;
block|}
name|ServerName
name|serverName
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|logDirName
operator|.
name|endsWith
argument_list|(
name|HLog
operator|.
name|SPLITTING_EXT
argument_list|)
condition|)
block|{
name|logDirName
operator|=
name|logDirName
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|logDirName
operator|.
name|length
argument_list|()
operator|-
name|HLog
operator|.
name|SPLITTING_EXT
operator|.
name|length
argument_list|()
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|serverName
operator|=
name|ServerName
operator|.
name|parseServerName
argument_list|(
name|logDirName
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|ex
parameter_list|)
block|{
name|serverName
operator|=
literal|null
expr_stmt|;
name|LOG
operator|.
name|warn
argument_list|(
literal|"Invalid log file path="
operator|+
name|logFile
argument_list|,
name|ex
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|serverName
operator|!=
literal|null
operator|&&
name|serverName
operator|.
name|getStartcode
argument_list|()
operator|<
literal|0
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Invalid log file path="
operator|+
name|logFile
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
return|return
name|serverName
return|;
block|}
comment|/**    * Returns sorted set of edit files made by wal-log splitter, excluding files    * with '.temp' suffix.    *    * @param fs    * @param regiondir    * @return Files in passed<code>regiondir</code> as a sorted set.    * @throws IOException    */
specifier|public
specifier|static
name|NavigableSet
argument_list|<
name|Path
argument_list|>
name|getSplitEditFilesSorted
parameter_list|(
specifier|final
name|FileSystem
name|fs
parameter_list|,
specifier|final
name|Path
name|regiondir
parameter_list|)
throws|throws
name|IOException
block|{
name|NavigableSet
argument_list|<
name|Path
argument_list|>
name|filesSorted
init|=
operator|new
name|TreeSet
argument_list|<
name|Path
argument_list|>
argument_list|()
decl_stmt|;
name|Path
name|editsdir
init|=
name|HLogUtil
operator|.
name|getRegionDirRecoveredEditsDir
argument_list|(
name|regiondir
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|fs
operator|.
name|exists
argument_list|(
name|editsdir
argument_list|)
condition|)
return|return
name|filesSorted
return|;
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
name|editsdir
argument_list|,
operator|new
name|PathFilter
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|accept
parameter_list|(
name|Path
name|p
parameter_list|)
block|{
name|boolean
name|result
init|=
literal|false
decl_stmt|;
try|try
block|{
comment|// Return files and only files that match the editfile names pattern.
comment|// There can be other files in this directory other than edit files.
comment|// In particular, on error, we'll move aside the bad edit file giving
comment|// it a timestamp suffix. See moveAsideBadEditsFile.
name|Matcher
name|m
init|=
name|HLog
operator|.
name|EDITFILES_NAME_PATTERN
operator|.
name|matcher
argument_list|(
name|p
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
name|result
operator|=
name|fs
operator|.
name|isFile
argument_list|(
name|p
argument_list|)
operator|&&
name|m
operator|.
name|matches
argument_list|()
expr_stmt|;
comment|// Skip the file whose name ends with RECOVERED_LOG_TMPFILE_SUFFIX,
comment|// because it means splithlog thread is writting this file.
if|if
condition|(
name|p
operator|.
name|getName
argument_list|()
operator|.
name|endsWith
argument_list|(
name|HLog
operator|.
name|RECOVERED_LOG_TMPFILE_SUFFIX
argument_list|)
condition|)
block|{
name|result
operator|=
literal|false
expr_stmt|;
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
literal|"Failed isFile check on "
operator|+
name|p
argument_list|)
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
block|}
argument_list|)
decl_stmt|;
if|if
condition|(
name|files
operator|==
literal|null
condition|)
return|return
name|filesSorted
return|;
for|for
control|(
name|FileStatus
name|status
range|:
name|files
control|)
block|{
name|filesSorted
operator|.
name|add
argument_list|(
name|status
operator|.
name|getPath
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|filesSorted
return|;
block|}
specifier|public
specifier|static
name|boolean
name|isMetaFile
parameter_list|(
name|Path
name|p
parameter_list|)
block|{
if|if
condition|(
name|p
operator|.
name|getName
argument_list|()
operator|.
name|endsWith
argument_list|(
name|HLog
operator|.
name|META_HLOG_FILE_EXTN
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
return|return
literal|false
return|;
block|}
comment|/**    * Write the marker that a compaction has succeeded and is about to be committed.    * This provides info to the HMaster to allow it to recover the compaction if    * this regionserver dies in the middle (This part is not yet implemented). It also prevents    * the compaction from finishing if this regionserver has already lost its lease on the log.    */
specifier|public
specifier|static
name|void
name|writeCompactionMarker
parameter_list|(
name|HLog
name|log
parameter_list|,
name|HTableDescriptor
name|htd
parameter_list|,
name|HRegionInfo
name|info
parameter_list|,
specifier|final
name|CompactionDescriptor
name|c
parameter_list|)
throws|throws
name|IOException
block|{
name|WALEdit
name|e
init|=
name|WALEdit
operator|.
name|createCompaction
argument_list|(
name|c
argument_list|)
decl_stmt|;
name|log
operator|.
name|append
argument_list|(
name|info
argument_list|,
name|c
operator|.
name|getTableName
argument_list|()
operator|.
name|toByteArray
argument_list|()
argument_list|,
name|e
argument_list|,
name|EnvironmentEdgeManager
operator|.
name|currentTimeMillis
argument_list|()
argument_list|,
name|htd
argument_list|)
expr_stmt|;
if|if
condition|(
name|LOG
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|trace
argument_list|(
literal|"Appended compaction marker "
operator|+
name|TextFormat
operator|.
name|shortDebugString
argument_list|(
name|c
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

