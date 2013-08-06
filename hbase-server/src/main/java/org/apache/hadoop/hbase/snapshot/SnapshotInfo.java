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
name|snapshot
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
name|FileNotFoundException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|text
operator|.
name|SimpleDateFormat
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
name|AtomicInteger
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
name|AtomicLong
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Date
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
name|Configured
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
name|util
operator|.
name|StringUtils
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
name|util
operator|.
name|Tool
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
name|util
operator|.
name|ToolRunner
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
name|hbase
operator|.
name|HBaseConfiguration
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
name|io
operator|.
name|HFileLink
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
name|io
operator|.
name|HLogLink
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
name|HBaseProtos
operator|.
name|SnapshotDescription
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
name|snapshot
operator|.
name|SnapshotDescriptionUtils
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
name|snapshot
operator|.
name|SnapshotReferenceUtil
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
name|FSTableDescriptors
import|;
end_import

begin_comment
comment|/**  * Tool for dumping snapshot information.  *<ol>  *<li> Table Descriptor  *<li> Snapshot creation time, type, format version, ...  *<li> List of hfiles and hlogs  *<li> Stats about hfiles and logs sizes, percentage of shared with the source table, ...  *</ol>  */
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
specifier|final
class|class
name|SnapshotInfo
extends|extends
name|Configured
implements|implements
name|Tool
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
name|SnapshotInfo
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/**    * Statistics about the snapshot    *<ol>    *<li> How many store files and logs are in the archive    *<li> How many store files and logs are shared with the table    *<li> Total store files and logs size and shared amount    *</ol>    */
specifier|public
specifier|static
class|class
name|SnapshotStats
block|{
comment|/** Information about the file referenced by the snapshot */
specifier|static
class|class
name|FileInfo
block|{
specifier|private
specifier|final
name|boolean
name|inArchive
decl_stmt|;
specifier|private
specifier|final
name|long
name|size
decl_stmt|;
name|FileInfo
parameter_list|(
specifier|final
name|boolean
name|inArchive
parameter_list|,
specifier|final
name|long
name|size
parameter_list|)
block|{
name|this
operator|.
name|inArchive
operator|=
name|inArchive
expr_stmt|;
name|this
operator|.
name|size
operator|=
name|size
expr_stmt|;
block|}
comment|/** @return true if the file is in the archive */
specifier|public
name|boolean
name|inArchive
parameter_list|()
block|{
return|return
name|this
operator|.
name|inArchive
return|;
block|}
comment|/** @return true if the file is missing */
specifier|public
name|boolean
name|isMissing
parameter_list|()
block|{
return|return
name|this
operator|.
name|size
operator|<
literal|0
return|;
block|}
comment|/** @return the file size */
specifier|public
name|long
name|getSize
parameter_list|()
block|{
return|return
name|this
operator|.
name|size
return|;
block|}
block|}
specifier|private
name|int
name|hfileArchiveCount
init|=
literal|0
decl_stmt|;
specifier|private
name|int
name|hfilesMissing
init|=
literal|0
decl_stmt|;
specifier|private
name|int
name|hfilesCount
init|=
literal|0
decl_stmt|;
specifier|private
name|int
name|logsMissing
init|=
literal|0
decl_stmt|;
specifier|private
name|int
name|logsCount
init|=
literal|0
decl_stmt|;
specifier|private
name|long
name|hfileArchiveSize
init|=
literal|0
decl_stmt|;
specifier|private
name|long
name|hfileSize
init|=
literal|0
decl_stmt|;
specifier|private
name|long
name|logSize
init|=
literal|0
decl_stmt|;
specifier|private
specifier|final
name|SnapshotDescription
name|snapshot
decl_stmt|;
specifier|private
specifier|final
name|Configuration
name|conf
decl_stmt|;
specifier|private
specifier|final
name|FileSystem
name|fs
decl_stmt|;
name|SnapshotStats
parameter_list|(
specifier|final
name|Configuration
name|conf
parameter_list|,
specifier|final
name|FileSystem
name|fs
parameter_list|,
specifier|final
name|SnapshotDescription
name|snapshot
parameter_list|)
block|{
name|this
operator|.
name|snapshot
operator|=
name|snapshot
expr_stmt|;
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|this
operator|.
name|fs
operator|=
name|fs
expr_stmt|;
block|}
comment|/** @return the snapshot descriptor */
specifier|public
name|SnapshotDescription
name|getSnapshotDescription
parameter_list|()
block|{
return|return
name|this
operator|.
name|snapshot
return|;
block|}
comment|/** @return true if the snapshot is corrupted */
specifier|public
name|boolean
name|isSnapshotCorrupted
parameter_list|()
block|{
return|return
name|hfilesMissing
operator|>
literal|0
operator|||
name|logsMissing
operator|>
literal|0
return|;
block|}
comment|/** @return the number of available store files */
specifier|public
name|int
name|getStoreFilesCount
parameter_list|()
block|{
return|return
name|hfilesCount
operator|+
name|hfileArchiveCount
return|;
block|}
comment|/** @return the number of available store files in the archive */
specifier|public
name|int
name|getArchivedStoreFilesCount
parameter_list|()
block|{
return|return
name|hfileArchiveCount
return|;
block|}
comment|/** @return the number of available log files */
specifier|public
name|int
name|getLogsCount
parameter_list|()
block|{
return|return
name|logsCount
return|;
block|}
comment|/** @return the number of missing store files */
specifier|public
name|int
name|getMissingStoreFilesCount
parameter_list|()
block|{
return|return
name|hfilesMissing
return|;
block|}
comment|/** @return the number of missing log files */
specifier|public
name|int
name|getMissingLogsCount
parameter_list|()
block|{
return|return
name|logsMissing
return|;
block|}
comment|/** @return the total size of the store files referenced by the snapshot */
specifier|public
name|long
name|getStoreFilesSize
parameter_list|()
block|{
return|return
name|hfileSize
operator|+
name|hfileArchiveSize
return|;
block|}
comment|/** @return the total size of the store files shared */
specifier|public
name|long
name|getSharedStoreFilesSize
parameter_list|()
block|{
return|return
name|hfileSize
return|;
block|}
comment|/** @return the total size of the store files in the archive */
specifier|public
name|long
name|getArchivedStoreFileSize
parameter_list|()
block|{
return|return
name|hfileArchiveSize
return|;
block|}
comment|/** @return the percentage of the shared store files */
specifier|public
name|float
name|getSharedStoreFilePercentage
parameter_list|()
block|{
return|return
operator|(
operator|(
name|float
operator|)
name|hfileSize
operator|/
operator|(
name|hfileSize
operator|+
name|hfileArchiveSize
operator|)
operator|)
operator|*
literal|100
return|;
block|}
comment|/** @return the total log size */
specifier|public
name|long
name|getLogsSize
parameter_list|()
block|{
return|return
name|logSize
return|;
block|}
comment|/**      * Add the specified store file to the stats      * @param region region encoded Name      * @param family family name      * @param hfile store file name      * @return the store file information      */
name|FileInfo
name|addStoreFile
parameter_list|(
specifier|final
name|String
name|region
parameter_list|,
specifier|final
name|String
name|family
parameter_list|,
specifier|final
name|String
name|hfile
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|table
init|=
name|this
operator|.
name|snapshot
operator|.
name|getTable
argument_list|()
decl_stmt|;
name|Path
name|path
init|=
operator|new
name|Path
argument_list|(
name|family
argument_list|,
name|HFileLink
operator|.
name|createHFileLinkName
argument_list|(
name|table
argument_list|,
name|region
argument_list|,
name|hfile
argument_list|)
argument_list|)
decl_stmt|;
name|HFileLink
name|link
init|=
operator|new
name|HFileLink
argument_list|(
name|conf
argument_list|,
name|path
argument_list|)
decl_stmt|;
name|boolean
name|inArchive
init|=
literal|false
decl_stmt|;
name|long
name|size
init|=
operator|-
literal|1
decl_stmt|;
try|try
block|{
if|if
condition|(
operator|(
name|inArchive
operator|=
name|fs
operator|.
name|exists
argument_list|(
name|link
operator|.
name|getArchivePath
argument_list|()
argument_list|)
operator|)
condition|)
block|{
name|size
operator|=
name|fs
operator|.
name|getFileStatus
argument_list|(
name|link
operator|.
name|getArchivePath
argument_list|()
argument_list|)
operator|.
name|getLen
argument_list|()
expr_stmt|;
name|hfileArchiveSize
operator|+=
name|size
expr_stmt|;
name|hfileArchiveCount
operator|++
expr_stmt|;
block|}
else|else
block|{
name|size
operator|=
name|link
operator|.
name|getFileStatus
argument_list|(
name|fs
argument_list|)
operator|.
name|getLen
argument_list|()
expr_stmt|;
name|hfileSize
operator|+=
name|size
expr_stmt|;
name|hfilesCount
operator|++
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|FileNotFoundException
name|e
parameter_list|)
block|{
name|hfilesMissing
operator|++
expr_stmt|;
block|}
return|return
operator|new
name|FileInfo
argument_list|(
name|inArchive
argument_list|,
name|size
argument_list|)
return|;
block|}
comment|/**      * Add the specified recovered.edits file to the stats      * @param region region encoded name      * @param logfile log file name      * @return the recovered.edits information      */
name|FileInfo
name|addRecoveredEdits
parameter_list|(
specifier|final
name|String
name|region
parameter_list|,
specifier|final
name|String
name|logfile
parameter_list|)
throws|throws
name|IOException
block|{
name|Path
name|rootDir
init|=
name|FSUtils
operator|.
name|getRootDir
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|Path
name|snapshotDir
init|=
name|SnapshotDescriptionUtils
operator|.
name|getCompletedSnapshotDir
argument_list|(
name|snapshot
argument_list|,
name|rootDir
argument_list|)
decl_stmt|;
name|Path
name|path
init|=
name|SnapshotReferenceUtil
operator|.
name|getRecoveredEdits
argument_list|(
name|snapshotDir
argument_list|,
name|region
argument_list|,
name|logfile
argument_list|)
decl_stmt|;
name|long
name|size
init|=
name|fs
operator|.
name|getFileStatus
argument_list|(
name|path
argument_list|)
operator|.
name|getLen
argument_list|()
decl_stmt|;
name|logSize
operator|+=
name|size
expr_stmt|;
name|logsCount
operator|++
expr_stmt|;
return|return
operator|new
name|FileInfo
argument_list|(
literal|true
argument_list|,
name|size
argument_list|)
return|;
block|}
comment|/**      * Add the specified log file to the stats      * @param server server name      * @param logfile log file name      * @return the log information      */
name|FileInfo
name|addLogFile
parameter_list|(
specifier|final
name|String
name|server
parameter_list|,
specifier|final
name|String
name|logfile
parameter_list|)
throws|throws
name|IOException
block|{
name|HLogLink
name|logLink
init|=
operator|new
name|HLogLink
argument_list|(
name|conf
argument_list|,
name|server
argument_list|,
name|logfile
argument_list|)
decl_stmt|;
name|long
name|size
init|=
operator|-
literal|1
decl_stmt|;
try|try
block|{
name|size
operator|=
name|logLink
operator|.
name|getFileStatus
argument_list|(
name|fs
argument_list|)
operator|.
name|getLen
argument_list|()
expr_stmt|;
name|logSize
operator|+=
name|size
expr_stmt|;
name|logsCount
operator|++
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|FileNotFoundException
name|e
parameter_list|)
block|{
name|logsMissing
operator|++
expr_stmt|;
block|}
return|return
operator|new
name|FileInfo
argument_list|(
literal|false
argument_list|,
name|size
argument_list|)
return|;
block|}
block|}
specifier|private
name|FileSystem
name|fs
decl_stmt|;
specifier|private
name|Path
name|rootDir
decl_stmt|;
specifier|private
name|HTableDescriptor
name|snapshotTableDesc
decl_stmt|;
specifier|private
name|SnapshotDescription
name|snapshotDesc
decl_stmt|;
specifier|private
name|Path
name|snapshotDir
decl_stmt|;
annotation|@
name|Override
specifier|public
name|int
name|run
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|String
name|snapshotName
init|=
literal|null
decl_stmt|;
name|boolean
name|showSchema
init|=
literal|false
decl_stmt|;
name|boolean
name|showFiles
init|=
literal|false
decl_stmt|;
name|boolean
name|showStats
init|=
literal|false
decl_stmt|;
comment|// Process command line args
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|args
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|String
name|cmd
init|=
name|args
index|[
name|i
index|]
decl_stmt|;
try|try
block|{
if|if
condition|(
name|cmd
operator|.
name|equals
argument_list|(
literal|"-snapshot"
argument_list|)
condition|)
block|{
name|snapshotName
operator|=
name|args
index|[
operator|++
name|i
index|]
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|cmd
operator|.
name|equals
argument_list|(
literal|"-files"
argument_list|)
condition|)
block|{
name|showFiles
operator|=
literal|true
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|cmd
operator|.
name|equals
argument_list|(
literal|"-stats"
argument_list|)
condition|)
block|{
name|showStats
operator|=
literal|true
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|cmd
operator|.
name|equals
argument_list|(
literal|"-schema"
argument_list|)
condition|)
block|{
name|showSchema
operator|=
literal|true
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|cmd
operator|.
name|equals
argument_list|(
literal|"-h"
argument_list|)
operator|||
name|cmd
operator|.
name|equals
argument_list|(
literal|"--help"
argument_list|)
condition|)
block|{
name|printUsageAndExit
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"UNEXPECTED: "
operator|+
name|cmd
argument_list|)
expr_stmt|;
name|printUsageAndExit
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
name|printUsageAndExit
argument_list|()
expr_stmt|;
block|}
block|}
if|if
condition|(
name|snapshotName
operator|==
literal|null
condition|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"Missing snapshot name!"
argument_list|)
expr_stmt|;
name|printUsageAndExit
argument_list|()
expr_stmt|;
return|return
literal|1
return|;
block|}
name|Configuration
name|conf
init|=
name|getConf
argument_list|()
decl_stmt|;
name|fs
operator|=
name|FileSystem
operator|.
name|get
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|rootDir
operator|=
name|FSUtils
operator|.
name|getRootDir
argument_list|(
name|conf
argument_list|)
expr_stmt|;
comment|// Load snapshot information
if|if
condition|(
operator|!
name|loadSnapshotInfo
argument_list|(
name|snapshotName
argument_list|)
condition|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"Snapshot '"
operator|+
name|snapshotName
operator|+
literal|"' not found!"
argument_list|)
expr_stmt|;
return|return
literal|1
return|;
block|}
name|printInfo
argument_list|()
expr_stmt|;
if|if
condition|(
name|showSchema
condition|)
name|printSchema
argument_list|()
expr_stmt|;
if|if
condition|(
name|showFiles
operator|||
name|showStats
condition|)
name|printFiles
argument_list|(
name|showFiles
argument_list|)
expr_stmt|;
return|return
literal|0
return|;
block|}
comment|/**    * Load snapshot info and table descriptor for the specified snapshot    * @param snapshotName name of the snapshot to load    * @return false if snapshot is not found    */
specifier|private
name|boolean
name|loadSnapshotInfo
parameter_list|(
specifier|final
name|String
name|snapshotName
parameter_list|)
throws|throws
name|IOException
block|{
name|snapshotDir
operator|=
name|SnapshotDescriptionUtils
operator|.
name|getCompletedSnapshotDir
argument_list|(
name|snapshotName
argument_list|,
name|rootDir
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|fs
operator|.
name|exists
argument_list|(
name|snapshotDir
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Snapshot '"
operator|+
name|snapshotName
operator|+
literal|"' not found in: "
operator|+
name|snapshotDir
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
name|snapshotDesc
operator|=
name|SnapshotDescriptionUtils
operator|.
name|readSnapshotInfo
argument_list|(
name|fs
argument_list|,
name|snapshotDir
argument_list|)
expr_stmt|;
name|snapshotTableDesc
operator|=
name|FSTableDescriptors
operator|.
name|getTableDescriptorFromFs
argument_list|(
name|fs
argument_list|,
name|snapshotDir
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
comment|/**    * Dump the {@link SnapshotDescription}    */
specifier|private
name|void
name|printInfo
parameter_list|()
block|{
name|SimpleDateFormat
name|df
init|=
operator|new
name|SimpleDateFormat
argument_list|(
literal|"yyyy-MM-dd'T'HH:mm:ss"
argument_list|)
decl_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Snapshot Info"
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"----------------------------------------"
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"   Name: "
operator|+
name|snapshotDesc
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"   Type: "
operator|+
name|snapshotDesc
operator|.
name|getType
argument_list|()
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"  Table: "
operator|+
name|snapshotDesc
operator|.
name|getTable
argument_list|()
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|" Format: "
operator|+
name|snapshotDesc
operator|.
name|getVersion
argument_list|()
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Created: "
operator|+
name|df
operator|.
name|format
argument_list|(
operator|new
name|Date
argument_list|(
name|snapshotDesc
operator|.
name|getCreationTime
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|()
expr_stmt|;
block|}
comment|/**    * Dump the {@link HTableDescriptor}    */
specifier|private
name|void
name|printSchema
parameter_list|()
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Table Descriptor"
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"----------------------------------------"
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
name|snapshotTableDesc
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|()
expr_stmt|;
block|}
comment|/**    * Collect the hfiles and logs statistics of the snapshot and    * dump the file list if requested and the collected information.    */
specifier|private
name|void
name|printFiles
parameter_list|(
specifier|final
name|boolean
name|showFiles
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|showFiles
condition|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Snapshot Files"
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"----------------------------------------"
argument_list|)
expr_stmt|;
block|}
comment|// Collect information about hfiles and logs in the snapshot
specifier|final
name|String
name|table
init|=
name|this
operator|.
name|snapshotDesc
operator|.
name|getTable
argument_list|()
decl_stmt|;
specifier|final
name|SnapshotStats
name|stats
init|=
operator|new
name|SnapshotStats
argument_list|(
name|this
operator|.
name|getConf
argument_list|()
argument_list|,
name|this
operator|.
name|fs
argument_list|,
name|this
operator|.
name|snapshotDesc
argument_list|)
decl_stmt|;
name|SnapshotReferenceUtil
operator|.
name|visitReferencedFiles
argument_list|(
name|fs
argument_list|,
name|snapshotDir
argument_list|,
operator|new
name|SnapshotReferenceUtil
operator|.
name|FileVisitor
argument_list|()
block|{
specifier|public
name|void
name|storeFile
parameter_list|(
specifier|final
name|String
name|region
parameter_list|,
specifier|final
name|String
name|family
parameter_list|,
specifier|final
name|String
name|hfile
parameter_list|)
throws|throws
name|IOException
block|{
name|SnapshotStats
operator|.
name|FileInfo
name|info
init|=
name|stats
operator|.
name|addStoreFile
argument_list|(
name|region
argument_list|,
name|family
argument_list|,
name|hfile
argument_list|)
decl_stmt|;
if|if
condition|(
name|showFiles
condition|)
block|{
name|System
operator|.
name|out
operator|.
name|printf
argument_list|(
literal|"%8s %s/%s/%s/%s %s%n"
argument_list|,
operator|(
name|info
operator|.
name|isMissing
argument_list|()
condition|?
literal|"-"
else|:
name|StringUtils
operator|.
name|humanReadableInt
argument_list|(
name|info
operator|.
name|getSize
argument_list|()
argument_list|)
operator|)
argument_list|,
name|table
argument_list|,
name|region
argument_list|,
name|family
argument_list|,
name|hfile
argument_list|,
operator|(
name|info
operator|.
name|inArchive
argument_list|()
condition|?
literal|"(archive)"
else|:
name|info
operator|.
name|isMissing
argument_list|()
condition|?
literal|"(NOT FOUND)"
else|:
literal|""
operator|)
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|recoveredEdits
parameter_list|(
specifier|final
name|String
name|region
parameter_list|,
specifier|final
name|String
name|logfile
parameter_list|)
throws|throws
name|IOException
block|{
name|SnapshotStats
operator|.
name|FileInfo
name|info
init|=
name|stats
operator|.
name|addRecoveredEdits
argument_list|(
name|region
argument_list|,
name|logfile
argument_list|)
decl_stmt|;
if|if
condition|(
name|showFiles
condition|)
block|{
name|System
operator|.
name|out
operator|.
name|printf
argument_list|(
literal|"%8s recovered.edits %s on region %s%n"
argument_list|,
name|StringUtils
operator|.
name|humanReadableInt
argument_list|(
name|info
operator|.
name|getSize
argument_list|()
argument_list|)
argument_list|,
name|logfile
argument_list|,
name|region
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|logFile
parameter_list|(
specifier|final
name|String
name|server
parameter_list|,
specifier|final
name|String
name|logfile
parameter_list|)
throws|throws
name|IOException
block|{
name|SnapshotStats
operator|.
name|FileInfo
name|info
init|=
name|stats
operator|.
name|addLogFile
argument_list|(
name|server
argument_list|,
name|logfile
argument_list|)
decl_stmt|;
if|if
condition|(
name|showFiles
condition|)
block|{
name|System
operator|.
name|out
operator|.
name|printf
argument_list|(
literal|"%8s log %s on server %s %s%n"
argument_list|,
operator|(
name|info
operator|.
name|isMissing
argument_list|()
condition|?
literal|"-"
else|:
name|StringUtils
operator|.
name|humanReadableInt
argument_list|(
name|info
operator|.
name|getSize
argument_list|()
argument_list|)
operator|)
argument_list|,
name|logfile
argument_list|,
name|server
argument_list|,
operator|(
name|info
operator|.
name|isMissing
argument_list|()
condition|?
literal|"(NOT FOUND)"
else|:
literal|""
operator|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
argument_list|)
expr_stmt|;
comment|// Dump the stats
name|System
operator|.
name|out
operator|.
name|println
argument_list|()
expr_stmt|;
if|if
condition|(
name|stats
operator|.
name|isSnapshotCorrupted
argument_list|()
condition|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"**************************************************************"
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|printf
argument_list|(
literal|"BAD SNAPSHOT: %d hfile(s) and %d log(s) missing.%n"
argument_list|,
name|stats
operator|.
name|getMissingStoreFilesCount
argument_list|()
argument_list|,
name|stats
operator|.
name|getMissingLogsCount
argument_list|()
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"**************************************************************"
argument_list|)
expr_stmt|;
block|}
name|System
operator|.
name|out
operator|.
name|printf
argument_list|(
literal|"%d HFiles (%d in archive), total size %s (%.2f%% %s shared with the source table)%n"
argument_list|,
name|stats
operator|.
name|getStoreFilesCount
argument_list|()
argument_list|,
name|stats
operator|.
name|getArchivedStoreFilesCount
argument_list|()
argument_list|,
name|StringUtils
operator|.
name|humanReadableInt
argument_list|(
name|stats
operator|.
name|getStoreFilesSize
argument_list|()
argument_list|)
argument_list|,
name|stats
operator|.
name|getSharedStoreFilePercentage
argument_list|()
argument_list|,
name|StringUtils
operator|.
name|humanReadableInt
argument_list|(
name|stats
operator|.
name|getSharedStoreFilesSize
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|printf
argument_list|(
literal|"%d Logs, total size %s%n"
argument_list|,
name|stats
operator|.
name|getLogsCount
argument_list|()
argument_list|,
name|StringUtils
operator|.
name|humanReadableInt
argument_list|(
name|stats
operator|.
name|getLogsSize
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|()
expr_stmt|;
block|}
specifier|private
name|void
name|printUsageAndExit
parameter_list|()
block|{
name|System
operator|.
name|err
operator|.
name|printf
argument_list|(
literal|"Usage: bin/hbase %s [options]%n"
argument_list|,
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|" where [options] are:"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"  -h|-help                Show this help and exit."
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"  -snapshot NAME          Snapshot to examine."
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"  -files                  Files and logs list."
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"  -stats                  Files and logs stats."
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"  -schema                 Describe the snapshotted table."
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|()
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"Examples:"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"  hbase "
operator|+
name|getClass
argument_list|()
operator|+
literal|" \\"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"    -snapshot MySnapshot -files"
argument_list|)
expr_stmt|;
name|System
operator|.
name|exit
argument_list|(
literal|1
argument_list|)
expr_stmt|;
block|}
comment|/**    * Returns the snapshot stats    * @param conf the {@link Configuration} to use    * @param snapshot {@link SnapshotDescription} to get stats from    * @return the snapshot stats    */
specifier|public
specifier|static
name|SnapshotStats
name|getSnapshotStats
parameter_list|(
specifier|final
name|Configuration
name|conf
parameter_list|,
specifier|final
name|SnapshotDescription
name|snapshot
parameter_list|)
throws|throws
name|IOException
block|{
name|Path
name|rootDir
init|=
name|FSUtils
operator|.
name|getRootDir
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|FileSystem
name|fs
init|=
name|FileSystem
operator|.
name|get
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|Path
name|snapshotDir
init|=
name|SnapshotDescriptionUtils
operator|.
name|getCompletedSnapshotDir
argument_list|(
name|snapshot
argument_list|,
name|rootDir
argument_list|)
decl_stmt|;
specifier|final
name|SnapshotStats
name|stats
init|=
operator|new
name|SnapshotStats
argument_list|(
name|conf
argument_list|,
name|fs
argument_list|,
name|snapshot
argument_list|)
decl_stmt|;
name|SnapshotReferenceUtil
operator|.
name|visitReferencedFiles
argument_list|(
name|fs
argument_list|,
name|snapshotDir
argument_list|,
operator|new
name|SnapshotReferenceUtil
operator|.
name|FileVisitor
argument_list|()
block|{
specifier|public
name|void
name|storeFile
parameter_list|(
specifier|final
name|String
name|region
parameter_list|,
specifier|final
name|String
name|family
parameter_list|,
specifier|final
name|String
name|hfile
parameter_list|)
throws|throws
name|IOException
block|{
name|stats
operator|.
name|addStoreFile
argument_list|(
name|region
argument_list|,
name|family
argument_list|,
name|hfile
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|recoveredEdits
parameter_list|(
specifier|final
name|String
name|region
parameter_list|,
specifier|final
name|String
name|logfile
parameter_list|)
throws|throws
name|IOException
block|{
name|stats
operator|.
name|addRecoveredEdits
argument_list|(
name|region
argument_list|,
name|logfile
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|logFile
parameter_list|(
specifier|final
name|String
name|server
parameter_list|,
specifier|final
name|String
name|logfile
parameter_list|)
throws|throws
name|IOException
block|{
name|stats
operator|.
name|addLogFile
argument_list|(
name|server
argument_list|,
name|logfile
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
return|return
name|stats
return|;
block|}
comment|/**    * The guts of the {@link #main} method.    * Call this method to avoid the {@link #main(String[])} System.exit.    * @param args    * @return errCode    * @throws Exception    */
specifier|static
name|int
name|innerMain
parameter_list|(
specifier|final
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|Exception
block|{
return|return
name|ToolRunner
operator|.
name|run
argument_list|(
name|HBaseConfiguration
operator|.
name|create
argument_list|()
argument_list|,
operator|new
name|SnapshotInfo
argument_list|()
argument_list|,
name|args
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|void
name|main
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|Exception
block|{
name|System
operator|.
name|exit
argument_list|(
name|innerMain
argument_list|(
name|args
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

