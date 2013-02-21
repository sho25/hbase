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
name|Closeable
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
name|util
operator|.
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|TreeMap
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
name|regionserver
operator|.
name|HRegion
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
name|HLogFactory
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
name|HLogKey
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
name|HLogUtil
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

begin_comment
comment|/**  * If the snapshot has references to one or more log files,  * those must be split (each log contains multiple tables and regions)  * and must be placed in the region/recovered.edits folder.  * (recovered.edits files will be played on region startup)  *  * In case of Restore: the log can just be split in the recovered.edits folder.  * In case of Clone: each entry in the log must be modified to use the new region name.  * (region names are encoded with: tableName, startKey, regionIdTimeStamp)  *  * We can't use the normal split code, because the HLogKey contains the  * table name and the region name, and in case of "clone from snapshot"  * region name and table name will be different and must be replaced in  * the recovered.edits.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
class|class
name|SnapshotLogSplitter
implements|implements
name|Closeable
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
name|SnapshotLogSplitter
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
class|class
name|LogWriter
implements|implements
name|Closeable
block|{
specifier|private
name|HLog
operator|.
name|Writer
name|writer
decl_stmt|;
specifier|private
name|Path
name|logFile
decl_stmt|;
specifier|private
name|long
name|seqId
decl_stmt|;
specifier|public
name|LogWriter
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
name|Path
name|logDir
parameter_list|,
name|long
name|seqId
parameter_list|)
throws|throws
name|IOException
block|{
name|logFile
operator|=
operator|new
name|Path
argument_list|(
name|logDir
argument_list|,
name|logFileName
argument_list|(
name|seqId
argument_list|,
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|writer
operator|=
name|HLogFactory
operator|.
name|createWriter
argument_list|(
name|fs
argument_list|,
name|logFile
argument_list|,
name|conf
argument_list|)
expr_stmt|;
name|this
operator|.
name|seqId
operator|=
name|seqId
expr_stmt|;
block|}
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{
name|writer
operator|.
name|close
argument_list|()
expr_stmt|;
name|Path
name|finalFile
init|=
operator|new
name|Path
argument_list|(
name|logFile
operator|.
name|getParent
argument_list|()
argument_list|,
name|logFileName
argument_list|(
name|seqId
argument_list|,
literal|false
argument_list|)
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"LogWriter tmpLogFile="
operator|+
name|logFile
operator|+
literal|" -> logFile="
operator|+
name|finalFile
argument_list|)
expr_stmt|;
name|fs
operator|.
name|rename
argument_list|(
name|logFile
argument_list|,
name|finalFile
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|append
parameter_list|(
specifier|final
name|HLog
operator|.
name|Entry
name|entry
parameter_list|)
throws|throws
name|IOException
block|{
name|writer
operator|.
name|append
argument_list|(
name|entry
argument_list|)
expr_stmt|;
if|if
condition|(
name|seqId
operator|<
name|entry
operator|.
name|getKey
argument_list|()
operator|.
name|getLogSeqNum
argument_list|()
condition|)
block|{
name|seqId
operator|=
name|entry
operator|.
name|getKey
argument_list|()
operator|.
name|getLogSeqNum
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
name|String
name|logFileName
parameter_list|(
name|long
name|seqId
parameter_list|,
name|boolean
name|temp
parameter_list|)
block|{
name|String
name|fileName
init|=
name|String
operator|.
name|format
argument_list|(
literal|"%019d"
argument_list|,
name|seqId
argument_list|)
decl_stmt|;
if|if
condition|(
name|temp
condition|)
name|fileName
operator|+=
name|HLog
operator|.
name|RECOVERED_LOG_TMPFILE_SUFFIX
expr_stmt|;
return|return
name|fileName
return|;
block|}
block|}
specifier|private
specifier|final
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|LogWriter
argument_list|>
name|regionLogWriters
init|=
operator|new
name|TreeMap
argument_list|<
name|byte
index|[]
argument_list|,
name|LogWriter
argument_list|>
argument_list|(
name|Bytes
operator|.
name|BYTES_COMPARATOR
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|byte
index|[]
argument_list|>
name|regionsMap
decl_stmt|;
specifier|private
specifier|final
name|Configuration
name|conf
decl_stmt|;
specifier|private
specifier|final
name|byte
index|[]
name|snapshotTableName
decl_stmt|;
specifier|private
specifier|final
name|byte
index|[]
name|tableName
decl_stmt|;
specifier|private
specifier|final
name|Path
name|tableDir
decl_stmt|;
specifier|private
specifier|final
name|FileSystem
name|fs
decl_stmt|;
comment|/**    * @params tableName snapshot table name    * @params regionsMap maps original region names to the new ones.    */
specifier|public
name|SnapshotLogSplitter
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
name|Path
name|tableDir
parameter_list|,
specifier|final
name|byte
index|[]
name|snapshotTableName
parameter_list|,
specifier|final
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|byte
index|[]
argument_list|>
name|regionsMap
parameter_list|)
block|{
name|this
operator|.
name|regionsMap
operator|=
name|regionsMap
expr_stmt|;
name|this
operator|.
name|snapshotTableName
operator|=
name|snapshotTableName
expr_stmt|;
name|this
operator|.
name|tableName
operator|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|tableDir
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|tableDir
operator|=
name|tableDir
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
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{
for|for
control|(
name|LogWriter
name|writer
range|:
name|regionLogWriters
operator|.
name|values
argument_list|()
control|)
block|{
name|writer
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|splitLog
parameter_list|(
specifier|final
name|String
name|serverName
parameter_list|,
specifier|final
name|String
name|logfile
parameter_list|)
throws|throws
name|IOException
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Restore log="
operator|+
name|logfile
operator|+
literal|" server="
operator|+
name|serverName
operator|+
literal|" for snapshotTable="
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|snapshotTableName
argument_list|)
operator|+
literal|" to table="
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|tableName
argument_list|)
argument_list|)
expr_stmt|;
name|splitLog
argument_list|(
operator|new
name|HLogLink
argument_list|(
name|conf
argument_list|,
name|serverName
argument_list|,
name|logfile
argument_list|)
operator|.
name|getAvailablePath
argument_list|(
name|fs
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|splitRecoveredEdit
parameter_list|(
specifier|final
name|Path
name|editPath
parameter_list|)
throws|throws
name|IOException
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Restore recover.edits="
operator|+
name|editPath
operator|+
literal|" for snapshotTable="
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|snapshotTableName
argument_list|)
operator|+
literal|" to table="
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|tableName
argument_list|)
argument_list|)
expr_stmt|;
name|splitLog
argument_list|(
name|editPath
argument_list|)
expr_stmt|;
block|}
comment|/**    * Split the snapshot HLog reference into regions recovered.edits.    *    * The HLogKey contains the table name and the region name,    * and they must be changed to the restored table names.    *    * @param logPath Snapshot HLog reference path    */
specifier|public
name|void
name|splitLog
parameter_list|(
specifier|final
name|Path
name|logPath
parameter_list|)
throws|throws
name|IOException
block|{
name|HLog
operator|.
name|Reader
name|log
init|=
name|HLogFactory
operator|.
name|createReader
argument_list|(
name|fs
argument_list|,
name|logPath
argument_list|,
name|conf
argument_list|)
decl_stmt|;
try|try
block|{
name|HLog
operator|.
name|Entry
name|entry
decl_stmt|;
name|LogWriter
name|writer
init|=
literal|null
decl_stmt|;
name|byte
index|[]
name|regionName
init|=
literal|null
decl_stmt|;
name|byte
index|[]
name|newRegionName
init|=
literal|null
decl_stmt|;
while|while
condition|(
operator|(
name|entry
operator|=
name|log
operator|.
name|next
argument_list|()
operator|)
operator|!=
literal|null
condition|)
block|{
name|HLogKey
name|key
init|=
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
comment|// We're interested only in the snapshot table that we're restoring
if|if
condition|(
operator|!
name|Bytes
operator|.
name|equals
argument_list|(
name|key
operator|.
name|getTablename
argument_list|()
argument_list|,
name|snapshotTableName
argument_list|)
condition|)
continue|continue;
comment|// Writer for region.
if|if
condition|(
operator|!
name|Bytes
operator|.
name|equals
argument_list|(
name|regionName
argument_list|,
name|key
operator|.
name|getEncodedRegionName
argument_list|()
argument_list|)
condition|)
block|{
name|regionName
operator|=
name|key
operator|.
name|getEncodedRegionName
argument_list|()
operator|.
name|clone
argument_list|()
expr_stmt|;
comment|// Get the new region name in case of clone, or use the original one
name|newRegionName
operator|=
name|regionsMap
operator|.
name|get
argument_list|(
name|regionName
argument_list|)
expr_stmt|;
if|if
condition|(
name|newRegionName
operator|==
literal|null
condition|)
name|newRegionName
operator|=
name|regionName
expr_stmt|;
name|writer
operator|=
name|getOrCreateWriter
argument_list|(
name|newRegionName
argument_list|,
name|key
operator|.
name|getLogSeqNum
argument_list|()
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"+ regionName="
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|regionName
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// Append Entry
name|key
operator|=
operator|new
name|HLogKey
argument_list|(
name|newRegionName
argument_list|,
name|tableName
argument_list|,
name|key
operator|.
name|getLogSeqNum
argument_list|()
argument_list|,
name|key
operator|.
name|getWriteTime
argument_list|()
argument_list|,
name|key
operator|.
name|getClusterId
argument_list|()
argument_list|)
expr_stmt|;
name|writer
operator|.
name|append
argument_list|(
operator|new
name|HLog
operator|.
name|Entry
argument_list|(
name|key
argument_list|,
name|entry
operator|.
name|getEdit
argument_list|()
argument_list|)
argument_list|)
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
literal|"Something wrong during the log split"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|log
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * Create a LogWriter for specified region if not already created.    */
specifier|private
name|LogWriter
name|getOrCreateWriter
parameter_list|(
specifier|final
name|byte
index|[]
name|regionName
parameter_list|,
name|long
name|seqId
parameter_list|)
throws|throws
name|IOException
block|{
name|LogWriter
name|writer
init|=
name|regionLogWriters
operator|.
name|get
argument_list|(
name|regionName
argument_list|)
decl_stmt|;
if|if
condition|(
name|writer
operator|==
literal|null
condition|)
block|{
name|Path
name|regionDir
init|=
name|HRegion
operator|.
name|getRegionDir
argument_list|(
name|tableDir
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|regionName
argument_list|)
argument_list|)
decl_stmt|;
name|Path
name|dir
init|=
name|HLogUtil
operator|.
name|getRegionDirRecoveredEditsDir
argument_list|(
name|regionDir
argument_list|)
decl_stmt|;
name|fs
operator|.
name|mkdirs
argument_list|(
name|dir
argument_list|)
expr_stmt|;
name|writer
operator|=
operator|new
name|LogWriter
argument_list|(
name|conf
argument_list|,
name|fs
argument_list|,
name|dir
argument_list|,
name|seqId
argument_list|)
expr_stmt|;
name|regionLogWriters
operator|.
name|put
argument_list|(
name|regionName
argument_list|,
name|writer
argument_list|)
expr_stmt|;
block|}
return|return
operator|(
name|writer
operator|)
return|;
block|}
block|}
end_class

end_unit

