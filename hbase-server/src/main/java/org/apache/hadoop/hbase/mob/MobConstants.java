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
name|mob
package|;
end_package

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
name|Tag
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
name|TagType
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
comment|/**  * The constants used in mob.  */
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
class|class
name|MobConstants
block|{
specifier|public
specifier|static
specifier|final
name|String
name|MOB_SCAN_RAW
init|=
literal|"hbase.mob.scan.raw"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|MOB_CACHE_BLOCKS
init|=
literal|"hbase.mob.cache.blocks"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|MOB_SCAN_REF_ONLY
init|=
literal|"hbase.mob.scan.ref.only"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|MOB_FILE_CACHE_SIZE_KEY
init|=
literal|"hbase.mob.file.cache.size"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|DEFAULT_MOB_FILE_CACHE_SIZE
init|=
literal|1000
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|MOB_DIR_NAME
init|=
literal|"mobdir"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|MOB_REGION_NAME
init|=
literal|".mob"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|byte
index|[]
name|MOB_REGION_NAME_BYTES
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|MOB_REGION_NAME
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|MOB_CLEANER_PERIOD
init|=
literal|"hbase.master.mob.ttl.cleaner.period"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|DEFAULT_MOB_CLEANER_PERIOD
init|=
literal|24
operator|*
literal|60
operator|*
literal|60
decl_stmt|;
comment|// one day
specifier|public
specifier|static
specifier|final
name|String
name|MOB_SWEEP_TOOL_COMPACTION_START_DATE
init|=
literal|"hbase.mob.sweep.tool.compaction.start.date"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|MOB_SWEEP_TOOL_COMPACTION_RATIO
init|=
literal|"hbase.mob.sweep.tool.compaction.ratio"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|MOB_SWEEP_TOOL_COMPACTION_MERGEABLE_SIZE
init|=
literal|"hbase.mob.sweep.tool.compaction.mergeable.size"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|float
name|DEFAULT_SWEEP_TOOL_MOB_COMPACTION_RATIO
init|=
literal|0.5f
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|long
name|DEFAULT_SWEEP_TOOL_MOB_COMPACTION_MERGEABLE_SIZE
init|=
literal|128
operator|*
literal|1024
operator|*
literal|1024
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|MOB_SWEEP_TOOL_COMPACTION_TEMP_DIR_NAME
init|=
literal|"mobcompaction"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|MOB_SWEEP_TOOL_COMPACTION_MEMSTORE_FLUSH_SIZE
init|=
literal|"hbase.mob.sweep.tool.compaction.memstore.flush.size"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|long
name|DEFAULT_MOB_SWEEP_TOOL_COMPACTION_MEMSTORE_FLUSH_SIZE
init|=
literal|1024
operator|*
literal|1024
operator|*
literal|128
decl_stmt|;
comment|// 128M
specifier|public
specifier|static
specifier|final
name|String
name|MOB_CACHE_EVICT_PERIOD
init|=
literal|"hbase.mob.cache.evict.period"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|MOB_CACHE_EVICT_REMAIN_RATIO
init|=
literal|"hbase.mob.cache.evict.remain.ratio"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|Tag
name|MOB_REF_TAG
init|=
operator|new
name|Tag
argument_list|(
name|TagType
operator|.
name|MOB_REFERENCE_TAG_TYPE
argument_list|,
name|HConstants
operator|.
name|EMPTY_BYTE_ARRAY
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|float
name|DEFAULT_EVICT_REMAIN_RATIO
init|=
literal|0.5f
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|long
name|DEFAULT_MOB_CACHE_EVICT_PERIOD
init|=
literal|3600l
decl_stmt|;
specifier|public
specifier|final
specifier|static
name|String
name|TEMP_DIR_NAME
init|=
literal|".tmp"
decl_stmt|;
specifier|public
specifier|final
specifier|static
name|String
name|BULKLOAD_DIR_NAME
init|=
literal|".bulkload"
decl_stmt|;
specifier|public
specifier|final
specifier|static
name|byte
index|[]
name|MOB_TABLE_LOCK_SUFFIX
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|".mobLock"
argument_list|)
decl_stmt|;
specifier|public
specifier|final
specifier|static
name|String
name|EMPTY_STRING
init|=
literal|""
decl_stmt|;
comment|/**    * If the size of a mob file is less than this value, it's regarded as a small file and needs to    * be merged in mob file compaction. The default value is 192MB.    */
specifier|public
specifier|static
specifier|final
name|String
name|MOB_FILE_COMPACTION_MERGEABLE_THRESHOLD
init|=
literal|"hbase.mob.file.compaction.mergeable.threshold"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|long
name|DEFAULT_MOB_FILE_COMPACTION_MERGEABLE_THRESHOLD
init|=
literal|192
operator|*
literal|1024
operator|*
literal|1024
decl_stmt|;
comment|/**    * The max number of del files that is allowed in the mob file compaction. In the mob file    * compaction, when the number of existing del files is larger than this value, they are merged    * until number of del files is not larger this value. The default value is 3.    */
specifier|public
specifier|static
specifier|final
name|String
name|MOB_DELFILE_MAX_COUNT
init|=
literal|"hbase.mob.delfile.max.count"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|DEFAULT_MOB_DELFILE_MAX_COUNT
init|=
literal|3
decl_stmt|;
comment|/**    * The max number of the mob files that is allowed in a batch of the mob file compaction.    * The mob file compaction merges the small mob files to bigger ones. If the number of the    * small files is very large, it could lead to a "too many opened file handlers" in the merge.    * And the merge has to be split into batches. This value limits the number of mob files    * that are selected in a batch of the mob file compaction. The default value is 100.    */
specifier|public
specifier|static
specifier|final
name|String
name|MOB_FILE_COMPACTION_BATCH_SIZE
init|=
literal|"hbase.mob.file.compaction.batch.size"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|DEFAULT_MOB_FILE_COMPACTION_BATCH_SIZE
init|=
literal|100
decl_stmt|;
comment|/**    * The period that MobFileCompactionChore runs. The unit is millisecond.    * The default value is one week.    */
specifier|public
specifier|static
specifier|final
name|String
name|MOB_FILE_COMPACTION_CHORE_PERIOD
init|=
literal|"hbase.mob.file.compaction.chore.period"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|DEFAULT_MOB_FILE_COMPACTION_CHORE_PERIOD
init|=
literal|24
operator|*
literal|60
operator|*
literal|60
operator|*
literal|7
decl_stmt|;
comment|// a week
specifier|public
specifier|static
specifier|final
name|String
name|MOB_FILE_COMPACTOR_CLASS_KEY
init|=
literal|"hbase.mob.file.compactor.class"
decl_stmt|;
comment|/**    * The max number of threads used in MobFileCompactor.    */
specifier|public
specifier|static
specifier|final
name|String
name|MOB_FILE_COMPACTION_THREADS_MAX
init|=
literal|"hbase.mob.file.compaction.threads.max"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|int
name|DEFAULT_MOB_FILE_COMPACTION_THREADS_MAX
init|=
literal|1
decl_stmt|;
specifier|private
name|MobConstants
parameter_list|()
block|{    }
block|}
end_class

end_unit

