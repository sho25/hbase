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
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collections
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
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicBoolean
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
name|hbase
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
name|hbase
operator|.
name|regionserver
operator|.
name|wal
operator|.
name|AbstractFSWAL
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
name|WALActionsListener
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|annotations
operator|.
name|VisibleForTesting
import|;
end_import

begin_comment
comment|/**  * Base class of a WAL Provider that returns a single thread safe WAL that writes to HDFS. By  * default, this implementation picks a directory in HDFS based on a combination of  *<ul>  *<li>the HBase root directory  *<li>HConstants.HREGION_LOGDIR_NAME  *<li>the given factory's factoryId (usually identifying the regionserver by host:port)  *</ul>  * It also uses the providerId to differentiate among files.  */
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
specifier|abstract
class|class
name|AbstractFSWALProvider
parameter_list|<
name|T
extends|extends
name|AbstractFSWAL
parameter_list|<
name|?
parameter_list|>
parameter_list|>
implements|implements
name|WALProvider
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
name|AbstractFSWALProvider
operator|.
name|class
argument_list|)
decl_stmt|;
comment|// Only public so classes back in regionserver.wal can access
specifier|public
interface|interface
name|Reader
extends|extends
name|WAL
operator|.
name|Reader
block|{
comment|/**      * @param fs File system.      * @param path Path.      * @param c Configuration.      * @param s Input stream that may have been pre-opened by the caller; may be null.      */
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
name|c
parameter_list|,
name|FSDataInputStream
name|s
parameter_list|)
throws|throws
name|IOException
function_decl|;
block|}
specifier|protected
specifier|volatile
name|T
name|wal
decl_stmt|;
specifier|protected
name|WALFactory
name|factory
init|=
literal|null
decl_stmt|;
specifier|protected
name|Configuration
name|conf
init|=
literal|null
decl_stmt|;
specifier|protected
name|List
argument_list|<
name|WALActionsListener
argument_list|>
name|listeners
init|=
literal|null
decl_stmt|;
specifier|protected
name|String
name|providerId
init|=
literal|null
decl_stmt|;
specifier|protected
name|AtomicBoolean
name|initialized
init|=
operator|new
name|AtomicBoolean
argument_list|(
literal|false
argument_list|)
decl_stmt|;
comment|// for default wal provider, logPrefix won't change
specifier|protected
name|String
name|logPrefix
init|=
literal|null
decl_stmt|;
comment|/**    * we synchronized on walCreateLock to prevent wal recreation in different threads    */
specifier|private
specifier|final
name|Object
name|walCreateLock
init|=
operator|new
name|Object
argument_list|()
decl_stmt|;
comment|/**    * @param factory factory that made us, identity used for FS layout. may not be null    * @param conf may not be null    * @param listeners may be null    * @param providerId differentiate between providers from one factory, used for FS layout. may be    *          null    */
annotation|@
name|Override
specifier|public
name|void
name|init
parameter_list|(
name|WALFactory
name|factory
parameter_list|,
name|Configuration
name|conf
parameter_list|,
name|List
argument_list|<
name|WALActionsListener
argument_list|>
name|listeners
parameter_list|,
name|String
name|providerId
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
operator|!
name|initialized
operator|.
name|compareAndSet
argument_list|(
literal|false
argument_list|,
literal|true
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"WALProvider.init should only be called once."
argument_list|)
throw|;
block|}
name|this
operator|.
name|factory
operator|=
name|factory
expr_stmt|;
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|this
operator|.
name|listeners
operator|=
name|listeners
expr_stmt|;
name|this
operator|.
name|providerId
operator|=
name|providerId
expr_stmt|;
comment|// get log prefix
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
operator|.
name|append
argument_list|(
name|factory
operator|.
name|factoryId
argument_list|)
decl_stmt|;
if|if
condition|(
name|providerId
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|providerId
operator|.
name|startsWith
argument_list|(
name|WAL_FILE_NAME_DELIMITER
argument_list|)
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
name|providerId
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|sb
operator|.
name|append
argument_list|(
name|WAL_FILE_NAME_DELIMITER
argument_list|)
operator|.
name|append
argument_list|(
name|providerId
argument_list|)
expr_stmt|;
block|}
block|}
name|logPrefix
operator|=
name|sb
operator|.
name|toString
argument_list|()
expr_stmt|;
name|doInit
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|WAL
argument_list|>
name|getWALs
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|wal
operator|==
literal|null
condition|)
block|{
return|return
name|Collections
operator|.
name|emptyList
argument_list|()
return|;
block|}
name|List
argument_list|<
name|WAL
argument_list|>
name|wals
init|=
operator|new
name|ArrayList
argument_list|<
name|WAL
argument_list|>
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|wals
operator|.
name|add
argument_list|(
name|wal
argument_list|)
expr_stmt|;
return|return
name|wals
return|;
block|}
annotation|@
name|Override
specifier|public
name|T
name|getWAL
parameter_list|(
name|byte
index|[]
name|identifier
parameter_list|,
name|byte
index|[]
name|namespace
parameter_list|)
throws|throws
name|IOException
block|{
name|T
name|walCopy
init|=
name|wal
decl_stmt|;
if|if
condition|(
name|walCopy
operator|==
literal|null
condition|)
block|{
comment|// only lock when need to create wal, and need to lock since
comment|// creating hlog on fs is time consuming
synchronized|synchronized
init|(
name|walCreateLock
init|)
block|{
name|walCopy
operator|=
name|wal
expr_stmt|;
if|if
condition|(
name|walCopy
operator|==
literal|null
condition|)
block|{
name|walCopy
operator|=
name|createWAL
argument_list|()
expr_stmt|;
name|wal
operator|=
name|walCopy
expr_stmt|;
block|}
block|}
block|}
return|return
name|walCopy
return|;
block|}
specifier|protected
specifier|abstract
name|T
name|createWAL
parameter_list|()
throws|throws
name|IOException
function_decl|;
specifier|protected
specifier|abstract
name|void
name|doInit
parameter_list|(
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
function_decl|;
annotation|@
name|Override
specifier|public
name|void
name|shutdown
parameter_list|()
throws|throws
name|IOException
block|{
name|T
name|log
init|=
name|this
operator|.
name|wal
decl_stmt|;
if|if
condition|(
name|log
operator|!=
literal|null
condition|)
block|{
name|log
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{
name|T
name|log
init|=
name|this
operator|.
name|wal
decl_stmt|;
if|if
condition|(
name|log
operator|!=
literal|null
condition|)
block|{
name|log
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * iff the given WALFactory is using the DefaultWALProvider for meta and/or non-meta, count the    * number of files (rolled and active). if either of them aren't, count 0 for that provider.    */
annotation|@
name|Override
specifier|public
name|long
name|getNumLogFiles
parameter_list|()
block|{
name|T
name|log
init|=
name|this
operator|.
name|wal
decl_stmt|;
return|return
name|log
operator|==
literal|null
condition|?
literal|0
else|:
name|log
operator|.
name|getNumLogFiles
argument_list|()
return|;
block|}
comment|/**    * iff the given WALFactory is using the DefaultWALProvider for meta and/or non-meta, count the    * size of files (only rolled). if either of them aren't, count 0 for that provider.    */
annotation|@
name|Override
specifier|public
name|long
name|getLogFileSize
parameter_list|()
block|{
name|T
name|log
init|=
name|this
operator|.
name|wal
decl_stmt|;
return|return
name|log
operator|==
literal|null
condition|?
literal|0
else|:
name|log
operator|.
name|getLogFileSize
argument_list|()
return|;
block|}
comment|/**    * returns the number of rolled WAL files.    */
annotation|@
name|VisibleForTesting
specifier|public
specifier|static
name|int
name|getNumRolledLogFiles
parameter_list|(
name|WAL
name|wal
parameter_list|)
block|{
return|return
operator|(
operator|(
name|AbstractFSWAL
argument_list|<
name|?
argument_list|>
operator|)
name|wal
operator|)
operator|.
name|getNumRolledLogFiles
argument_list|()
return|;
block|}
comment|/**    * returns the size of rolled WAL files.    */
annotation|@
name|VisibleForTesting
specifier|public
specifier|static
name|long
name|getLogFileSize
parameter_list|(
name|WAL
name|wal
parameter_list|)
block|{
return|return
operator|(
operator|(
name|AbstractFSWAL
argument_list|<
name|?
argument_list|>
operator|)
name|wal
operator|)
operator|.
name|getLogFileSize
argument_list|()
return|;
block|}
comment|/**    * return the current filename from the current wal.    */
annotation|@
name|VisibleForTesting
specifier|public
specifier|static
name|Path
name|getCurrentFileName
parameter_list|(
specifier|final
name|WAL
name|wal
parameter_list|)
block|{
return|return
operator|(
operator|(
name|AbstractFSWAL
argument_list|<
name|?
argument_list|>
operator|)
name|wal
operator|)
operator|.
name|getCurrentFileName
argument_list|()
return|;
block|}
comment|/**    * request a log roll, but don't actually do it.    */
annotation|@
name|VisibleForTesting
specifier|static
name|void
name|requestLogRoll
parameter_list|(
specifier|final
name|WAL
name|wal
parameter_list|)
block|{
operator|(
operator|(
name|AbstractFSWAL
argument_list|<
name|?
argument_list|>
operator|)
name|wal
operator|)
operator|.
name|requestLogRoll
argument_list|()
expr_stmt|;
block|}
comment|// should be package private; more visible for use in AbstractFSWAL
specifier|public
specifier|static
specifier|final
name|String
name|WAL_FILE_NAME_DELIMITER
init|=
literal|"."
decl_stmt|;
comment|/** The hbase:meta region's WAL filename extension */
annotation|@
name|VisibleForTesting
specifier|public
specifier|static
specifier|final
name|String
name|META_WAL_PROVIDER_ID
init|=
literal|".meta"
decl_stmt|;
specifier|static
specifier|final
name|String
name|DEFAULT_PROVIDER_ID
init|=
literal|"default"
decl_stmt|;
comment|// Implementation details that currently leak in tests or elsewhere follow
comment|/** File Extension used while splitting an WAL into regions (HBASE-2312) */
specifier|public
specifier|static
specifier|final
name|String
name|SPLITTING_EXT
init|=
literal|"-splitting"
decl_stmt|;
comment|/**    * It returns the file create timestamp from the file name. For name format see    * {@link #validateWALFilename(String)} public until remaining tests move to o.a.h.h.wal    * @param wal must not be null    * @return the file number that is part of the WAL file name    */
annotation|@
name|VisibleForTesting
specifier|public
specifier|static
name|long
name|extractFileNumFromWAL
parameter_list|(
specifier|final
name|WAL
name|wal
parameter_list|)
block|{
specifier|final
name|Path
name|walName
init|=
operator|(
operator|(
name|AbstractFSWAL
argument_list|<
name|?
argument_list|>
operator|)
name|wal
operator|)
operator|.
name|getCurrentFileName
argument_list|()
decl_stmt|;
if|if
condition|(
name|walName
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"The WAL path couldn't be null"
argument_list|)
throw|;
block|}
specifier|final
name|String
index|[]
name|walPathStrs
init|=
name|walName
operator|.
name|toString
argument_list|()
operator|.
name|split
argument_list|(
literal|"\\"
operator|+
name|WAL_FILE_NAME_DELIMITER
argument_list|)
decl_stmt|;
return|return
name|Long
operator|.
name|parseLong
argument_list|(
name|walPathStrs
index|[
name|walPathStrs
operator|.
name|length
operator|-
operator|(
name|isMetaFile
argument_list|(
name|walName
argument_list|)
condition|?
literal|2
else|:
literal|1
operator|)
index|]
argument_list|)
return|;
block|}
comment|/**    * Pattern used to validate a WAL file name see {@link #validateWALFilename(String)} for    * description.    */
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
name|META_WAL_PROVIDER_ID
operator|+
literal|")*"
argument_list|)
decl_stmt|;
comment|/**    * A WAL file name is of the format:&lt;wal-name&gt;{@link #WAL_FILE_NAME_DELIMITER}    *&lt;file-creation-timestamp&gt;[.meta]. provider-name is usually made up of a server-name and a    * provider-id    * @param filename name of the file to validate    * @return<tt>true</tt> if the filename matches an WAL,<tt>false</tt> otherwise    */
specifier|public
specifier|static
name|boolean
name|validateWALFilename
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
comment|/**    * Construct the directory name for all WALs on a given server.    * @param serverName Server name formatted as described in {@link ServerName}    * @return the relative WAL directory name, e.g.<code>.logs/1.example.org,60030,12345</code> if    *<code>serverName</code> passed is<code>1.example.org,60030,12345</code>    */
specifier|public
specifier|static
name|String
name|getWALDirectoryName
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
comment|/**    * Pulls a ServerName out of a Path generated according to our layout rules. In the below layouts,    * this method ignores the format of the logfile component. Current format: [base directory for    * hbase]/hbase/.logs/ServerName/logfile or [base directory for    * hbase]/hbase/.logs/ServerName-splitting/logfile Expected to work for individual log files and    * server-specific directories.    * @return null if it's not a log file. Returns the ServerName of the region server that created    *         this log file otherwise.    */
specifier|public
specifier|static
name|ServerName
name|getServerNameFromWALDirectoryName
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
block|{
name|startPathSB
operator|.
name|append
argument_list|(
literal|'/'
argument_list|)
expr_stmt|;
block|}
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
block|{
name|startPathSB
operator|.
name|append
argument_list|(
literal|'/'
argument_list|)
expr_stmt|;
block|}
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
name|Path
name|p
init|=
operator|new
name|Path
argument_list|(
name|path
argument_list|)
decl_stmt|;
return|return
name|getServerNameFromWALDirectoryName
argument_list|(
name|p
argument_list|)
return|;
block|}
comment|/**    * This function returns region server name from a log file name which is in one of the following    * formats:    *<ul>    *<li>hdfs://&lt;name node&gt;/hbase/.logs/&lt;server name&gt;-splitting/...</li>    *<li>hdfs://&lt;name node&gt;/hbase/.logs/&lt;server name&gt;/...</li>    *</ul>    * @return null if the passed in logFile isn't a valid WAL file path    */
specifier|public
specifier|static
name|ServerName
name|getServerNameFromWALDirectoryName
parameter_list|(
name|Path
name|logFile
parameter_list|)
block|{
name|String
name|logDirName
init|=
name|logFile
operator|.
name|getParent
argument_list|()
operator|.
name|getName
argument_list|()
decl_stmt|;
comment|// We were passed the directory and not a file in it.
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
name|logDirName
operator|=
name|logFile
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
literal|"Cannot parse a server name from path="
operator|+
name|logFile
operator|+
literal|"; "
operator|+
name|ex
operator|.
name|getMessage
argument_list|()
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
name|serverName
operator|=
literal|null
expr_stmt|;
block|}
return|return
name|serverName
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
return|return
name|isMetaFile
argument_list|(
name|p
operator|.
name|getName
argument_list|()
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|boolean
name|isMetaFile
parameter_list|(
name|String
name|p
parameter_list|)
block|{
if|if
condition|(
name|p
operator|!=
literal|null
operator|&&
name|p
operator|.
name|endsWith
argument_list|(
name|META_WAL_PROVIDER_ID
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
comment|/**    * Get prefix of the log from its name, assuming WAL name in format of    * log_prefix.filenumber.log_suffix    * @param name Name of the WAL to parse    * @return prefix of the log    * @see AbstractFSWAL#getCurrentFileName()    */
specifier|public
specifier|static
name|String
name|getWALPrefixFromWALName
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|int
name|endIndex
init|=
name|name
operator|.
name|replaceAll
argument_list|(
name|META_WAL_PROVIDER_ID
argument_list|,
literal|""
argument_list|)
operator|.
name|lastIndexOf
argument_list|(
literal|"."
argument_list|)
decl_stmt|;
return|return
name|name
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|endIndex
argument_list|)
return|;
block|}
block|}
end_class

end_unit

