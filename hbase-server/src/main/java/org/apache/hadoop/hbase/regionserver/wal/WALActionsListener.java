begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|org
operator|.
name|apache
operator|.
name|yetus
operator|.
name|audience
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
name|wal
operator|.
name|WALEdit
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
name|wal
operator|.
name|WALKey
import|;
end_import

begin_comment
comment|/**  * Get notification of WAL events. The invocations are inline  * so make sure your implementation is fast else you'll slow hbase.  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
interface|interface
name|WALActionsListener
block|{
comment|/**    * The WAL is going to be rolled. The oldPath can be null if this is    * the first log file from the regionserver.    * @param oldPath the path to the old wal    * @param newPath the path to the new wal    */
name|void
name|preLogRoll
parameter_list|(
name|Path
name|oldPath
parameter_list|,
name|Path
name|newPath
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * The WAL has been rolled. The oldPath can be null if this is    * the first log file from the regionserver.    * @param oldPath the path to the old wal    * @param newPath the path to the new wal    */
name|void
name|postLogRoll
parameter_list|(
name|Path
name|oldPath
parameter_list|,
name|Path
name|newPath
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * The WAL is going to be archived.    * @param oldPath the path to the old wal    * @param newPath the path to the new wal    */
name|void
name|preLogArchive
parameter_list|(
name|Path
name|oldPath
parameter_list|,
name|Path
name|newPath
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * The WAL has been archived.    * @param oldPath the path to the old wal    * @param newPath the path to the new wal    */
name|void
name|postLogArchive
parameter_list|(
name|Path
name|oldPath
parameter_list|,
name|Path
name|newPath
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * A request was made that the WAL be rolled.    */
name|void
name|logRollRequested
parameter_list|(
name|boolean
name|tooFewReplicas
parameter_list|)
function_decl|;
comment|/**    * The WAL is about to close.    */
name|void
name|logCloseRequested
parameter_list|()
function_decl|;
comment|/**   * Called before each write.   * @param info   * @param logKey   * @param logEdit   */
name|void
name|visitLogEntryBeforeWrite
parameter_list|(
name|HRegionInfo
name|info
parameter_list|,
name|WALKey
name|logKey
parameter_list|,
name|WALEdit
name|logEdit
parameter_list|)
function_decl|;
comment|/**    * @param logKey    * @param logEdit TODO: Retire this in favor of    *          {@link #visitLogEntryBeforeWrite(HRegionInfo, WALKey, WALEdit)} It only exists to get    *          scope when replicating. Scope should be in the WALKey and not need us passing in a    *<code>htd</code>.    * @throws IOException If failed to parse the WALEdit    */
name|void
name|visitLogEntryBeforeWrite
parameter_list|(
name|WALKey
name|logKey
parameter_list|,
name|WALEdit
name|logEdit
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * For notification post append to the writer.  Used by metrics system at least.    * TODO: Combine this with above.    * @param entryLen approx length of cells in this append.    * @param elapsedTimeMillis elapsed time in milliseconds.    * @param logKey A WAL key    * @param logEdit A WAL edit containing list of cells.    * @throws IOException if any network or I/O error occurred    */
name|void
name|postAppend
parameter_list|(
specifier|final
name|long
name|entryLen
parameter_list|,
specifier|final
name|long
name|elapsedTimeMillis
parameter_list|,
specifier|final
name|WALKey
name|logKey
parameter_list|,
specifier|final
name|WALEdit
name|logEdit
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * For notification post writer sync.  Used by metrics system at least.    * @param timeInNanos How long the filesystem sync took in nanoseconds.    * @param handlerSyncs How many sync handler calls were released by this call to filesystem    * sync.    */
name|void
name|postSync
parameter_list|(
specifier|final
name|long
name|timeInNanos
parameter_list|,
specifier|final
name|int
name|handlerSyncs
parameter_list|)
function_decl|;
specifier|static
class|class
name|Base
implements|implements
name|WALActionsListener
block|{
annotation|@
name|Override
specifier|public
name|void
name|preLogRoll
parameter_list|(
name|Path
name|oldPath
parameter_list|,
name|Path
name|newPath
parameter_list|)
throws|throws
name|IOException
block|{}
annotation|@
name|Override
specifier|public
name|void
name|postLogRoll
parameter_list|(
name|Path
name|oldPath
parameter_list|,
name|Path
name|newPath
parameter_list|)
throws|throws
name|IOException
block|{}
annotation|@
name|Override
specifier|public
name|void
name|preLogArchive
parameter_list|(
name|Path
name|oldPath
parameter_list|,
name|Path
name|newPath
parameter_list|)
throws|throws
name|IOException
block|{}
annotation|@
name|Override
specifier|public
name|void
name|postLogArchive
parameter_list|(
name|Path
name|oldPath
parameter_list|,
name|Path
name|newPath
parameter_list|)
throws|throws
name|IOException
block|{}
annotation|@
name|Override
specifier|public
name|void
name|logRollRequested
parameter_list|(
name|boolean
name|tooFewReplicas
parameter_list|)
block|{}
annotation|@
name|Override
specifier|public
name|void
name|logCloseRequested
parameter_list|()
block|{}
annotation|@
name|Override
specifier|public
name|void
name|visitLogEntryBeforeWrite
parameter_list|(
name|HRegionInfo
name|info
parameter_list|,
name|WALKey
name|logKey
parameter_list|,
name|WALEdit
name|logEdit
parameter_list|)
block|{}
annotation|@
name|Override
specifier|public
name|void
name|visitLogEntryBeforeWrite
parameter_list|(
name|WALKey
name|logKey
parameter_list|,
name|WALEdit
name|logEdit
parameter_list|)
throws|throws
name|IOException
block|{     }
annotation|@
name|Override
specifier|public
name|void
name|postAppend
parameter_list|(
specifier|final
name|long
name|entryLen
parameter_list|,
specifier|final
name|long
name|elapsedTimeMillis
parameter_list|,
specifier|final
name|WALKey
name|logKey
parameter_list|,
specifier|final
name|WALEdit
name|logEdit
parameter_list|)
throws|throws
name|IOException
block|{     }
annotation|@
name|Override
specifier|public
name|void
name|postSync
parameter_list|(
specifier|final
name|long
name|timeInNanos
parameter_list|,
specifier|final
name|int
name|handlerSyncs
parameter_list|)
block|{}
block|}
block|}
end_interface

end_unit

