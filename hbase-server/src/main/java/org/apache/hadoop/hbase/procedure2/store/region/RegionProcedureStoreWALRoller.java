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
name|procedure2
operator|.
name|store
operator|.
name|region
package|;
end_package

begin_import
import|import static
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|HConstants
operator|.
name|HREGION_OLDLOGDIR_NAME
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
name|concurrent
operator|.
name|TimeUnit
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
name|Abortable
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
name|master
operator|.
name|procedure
operator|.
name|MasterProcedureUtil
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
name|WALUtil
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
name|AbstractFSWALProvider
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
name|AbstractWALRoller
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
name|WAL
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
name|WALFactory
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
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
import|;
end_import

begin_comment
comment|/**  * As long as there is no RegionServerServices for the procedure store region, we need implement log  * roller logic by our own.  *<p/>  * We can reuse most of the code for normal wal roller, the only difference is that there is only  * one region, so in {@link #scheduleFlush(String)} method we can just schedule flush for the  * procedure store region.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|final
class|class
name|RegionProcedureStoreWALRoller
extends|extends
name|AbstractWALRoller
argument_list|<
name|Abortable
argument_list|>
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|RegionProcedureStoreWALRoller
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|static
specifier|final
name|String
name|ROLL_PERIOD_MS_KEY
init|=
literal|"hbase.procedure.store.region.walroll.period.ms"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|long
name|DEFAULT_ROLL_PERIOD_MS
init|=
name|TimeUnit
operator|.
name|MINUTES
operator|.
name|toMillis
argument_list|(
literal|15
argument_list|)
decl_stmt|;
specifier|private
specifier|volatile
name|RegionFlusherAndCompactor
name|flusherAndCompactor
decl_stmt|;
specifier|private
specifier|final
name|FileSystem
name|fs
decl_stmt|;
specifier|private
specifier|final
name|Path
name|walArchiveDir
decl_stmt|;
specifier|private
specifier|final
name|Path
name|globalWALArchiveDir
decl_stmt|;
specifier|private
name|RegionProcedureStoreWALRoller
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|Abortable
name|abortable
parameter_list|,
name|FileSystem
name|fs
parameter_list|,
name|Path
name|walRootDir
parameter_list|,
name|Path
name|globalWALRootDir
parameter_list|)
block|{
name|super
argument_list|(
literal|"RegionProcedureStoreWALRoller"
argument_list|,
name|conf
argument_list|,
name|abortable
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
name|walArchiveDir
operator|=
operator|new
name|Path
argument_list|(
name|walRootDir
argument_list|,
name|HREGION_OLDLOGDIR_NAME
argument_list|)
expr_stmt|;
name|this
operator|.
name|globalWALArchiveDir
operator|=
operator|new
name|Path
argument_list|(
name|globalWALRootDir
argument_list|,
name|HREGION_OLDLOGDIR_NAME
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|afterRoll
parameter_list|(
name|WAL
name|wal
parameter_list|)
block|{
comment|// move the archived WAL files to the global archive path
try|try
block|{
if|if
condition|(
operator|!
name|fs
operator|.
name|exists
argument_list|(
name|globalWALArchiveDir
argument_list|)
operator|&&
operator|!
name|fs
operator|.
name|mkdirs
argument_list|(
name|globalWALArchiveDir
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Failed to create global archive dir {}"
argument_list|,
name|globalWALArchiveDir
argument_list|)
expr_stmt|;
return|return;
block|}
name|FileStatus
index|[]
name|archivedWALFiles
init|=
name|fs
operator|.
name|listStatus
argument_list|(
name|walArchiveDir
argument_list|)
decl_stmt|;
if|if
condition|(
name|archivedWALFiles
operator|==
literal|null
condition|)
block|{
return|return;
block|}
for|for
control|(
name|FileStatus
name|status
range|:
name|archivedWALFiles
control|)
block|{
name|Path
name|file
init|=
name|status
operator|.
name|getPath
argument_list|()
decl_stmt|;
name|Path
name|newFile
init|=
operator|new
name|Path
argument_list|(
name|globalWALArchiveDir
argument_list|,
name|file
operator|.
name|getName
argument_list|()
operator|+
name|MasterProcedureUtil
operator|.
name|ARCHIVED_PROC_WAL_SUFFIX
argument_list|)
decl_stmt|;
if|if
condition|(
name|fs
operator|.
name|rename
argument_list|(
name|file
argument_list|,
name|newFile
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Successfully moved {} to {}"
argument_list|,
name|file
argument_list|,
name|newFile
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Failed to move archived wal from {} to global place {}"
argument_list|,
name|file
argument_list|,
name|newFile
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
literal|"Failed to move archived wals from {} to global dir {}"
argument_list|,
name|walArchiveDir
argument_list|,
name|globalWALArchiveDir
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|protected
name|void
name|scheduleFlush
parameter_list|(
name|String
name|encodedRegionName
parameter_list|)
block|{
name|RegionFlusherAndCompactor
name|flusher
init|=
name|this
operator|.
name|flusherAndCompactor
decl_stmt|;
if|if
condition|(
name|flusher
operator|!=
literal|null
condition|)
block|{
name|flusher
operator|.
name|requestFlush
argument_list|()
expr_stmt|;
block|}
block|}
name|void
name|setFlusherAndCompactor
parameter_list|(
name|RegionFlusherAndCompactor
name|flusherAndCompactor
parameter_list|)
block|{
name|this
operator|.
name|flusherAndCompactor
operator|=
name|flusherAndCompactor
expr_stmt|;
block|}
specifier|static
name|RegionProcedureStoreWALRoller
name|create
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|Abortable
name|abortable
parameter_list|,
name|FileSystem
name|fs
parameter_list|,
name|Path
name|walRootDir
parameter_list|,
name|Path
name|globalWALRootDir
parameter_list|)
block|{
comment|// we can not run with wal disabled, so force set it to true.
name|conf
operator|.
name|setBoolean
argument_list|(
name|WALFactory
operator|.
name|WAL_ENABLED
argument_list|,
literal|true
argument_list|)
expr_stmt|;
comment|// we do not need this feature, so force disable it.
name|conf
operator|.
name|setBoolean
argument_list|(
name|AbstractFSWALProvider
operator|.
name|SEPARATE_OLDLOGDIR
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setLong
argument_list|(
name|WAL_ROLL_PERIOD_KEY
argument_list|,
name|conf
operator|.
name|getLong
argument_list|(
name|ROLL_PERIOD_MS_KEY
argument_list|,
name|DEFAULT_ROLL_PERIOD_MS
argument_list|)
argument_list|)
expr_stmt|;
name|long
name|flushSize
init|=
name|conf
operator|.
name|getLong
argument_list|(
name|RegionFlusherAndCompactor
operator|.
name|FLUSH_SIZE_KEY
argument_list|,
name|RegionFlusherAndCompactor
operator|.
name|DEFAULT_FLUSH_SIZE
argument_list|)
decl_stmt|;
comment|// make the roll size the same with the flush size, as we only have one region here
name|conf
operator|.
name|setLong
argument_list|(
name|WALUtil
operator|.
name|WAL_BLOCK_SIZE
argument_list|,
name|flushSize
operator|*
literal|2
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setFloat
argument_list|(
name|AbstractFSWAL
operator|.
name|WAL_ROLL_MULTIPLIER
argument_list|,
literal|0.5f
argument_list|)
expr_stmt|;
return|return
operator|new
name|RegionProcedureStoreWALRoller
argument_list|(
name|conf
argument_list|,
name|abortable
argument_list|,
name|fs
argument_list|,
name|walRootDir
argument_list|,
name|globalWALRootDir
argument_list|)
return|;
block|}
block|}
end_class

end_unit

