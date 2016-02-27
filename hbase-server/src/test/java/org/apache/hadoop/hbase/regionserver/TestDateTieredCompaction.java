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
package|;
end_package

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|ImmutableList
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
name|collect
operator|.
name|Lists
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
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
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
name|hadoop
operator|.
name|hbase
operator|.
name|regionserver
operator|.
name|compactions
operator|.
name|CompactionConfiguration
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
name|compactions
operator|.
name|DateTieredCompactionPolicy
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
name|testclassification
operator|.
name|SmallTests
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Assert
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|experimental
operator|.
name|categories
operator|.
name|Category
import|;
end_import

begin_class
annotation|@
name|Category
argument_list|(
name|SmallTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestDateTieredCompaction
extends|extends
name|TestCompactionPolicy
block|{
name|ArrayList
argument_list|<
name|StoreFile
argument_list|>
name|sfCreate
parameter_list|(
name|long
index|[]
name|minTimestamps
parameter_list|,
name|long
index|[]
name|maxTimestamps
parameter_list|,
name|long
index|[]
name|sizes
parameter_list|)
throws|throws
name|IOException
block|{
name|ArrayList
argument_list|<
name|Long
argument_list|>
name|ageInDisk
init|=
operator|new
name|ArrayList
argument_list|<
name|Long
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|sizes
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|ageInDisk
operator|.
name|add
argument_list|(
literal|0L
argument_list|)
expr_stmt|;
block|}
name|ArrayList
argument_list|<
name|StoreFile
argument_list|>
name|ret
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|sizes
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|MockStoreFile
name|msf
init|=
operator|new
name|MockStoreFile
argument_list|(
name|TEST_UTIL
argument_list|,
name|TEST_FILE
argument_list|,
name|sizes
index|[
name|i
index|]
argument_list|,
name|ageInDisk
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
literal|false
argument_list|,
name|i
argument_list|)
decl_stmt|;
name|msf
operator|.
name|setTimeRangeTracker
argument_list|(
operator|new
name|TimeRangeTracker
argument_list|(
name|minTimestamps
index|[
name|i
index|]
argument_list|,
name|maxTimestamps
index|[
name|i
index|]
argument_list|)
argument_list|)
expr_stmt|;
name|ret
operator|.
name|add
argument_list|(
name|msf
argument_list|)
expr_stmt|;
block|}
return|return
name|ret
return|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|config
parameter_list|()
block|{
name|super
operator|.
name|config
argument_list|()
expr_stmt|;
comment|// Set up policy
name|conf
operator|.
name|setLong
argument_list|(
name|CompactionConfiguration
operator|.
name|MAX_AGE_MILLIS_KEY
argument_list|,
literal|100
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setLong
argument_list|(
name|CompactionConfiguration
operator|.
name|INCOMING_WINDOW_MIN_KEY
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setLong
argument_list|(
name|CompactionConfiguration
operator|.
name|BASE_WINDOW_MILLIS_KEY
argument_list|,
literal|6
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
name|CompactionConfiguration
operator|.
name|WINDOWS_PER_TIER_KEY
argument_list|,
literal|4
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|DefaultStoreEngine
operator|.
name|DEFAULT_COMPACTION_POLICY_CLASS_KEY
argument_list|,
name|DateTieredCompactionPolicy
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
comment|// Special settings for compaction policy per window
name|this
operator|.
name|conf
operator|.
name|setInt
argument_list|(
name|CompactionConfiguration
operator|.
name|HBASE_HSTORE_COMPACTION_MIN_KEY
argument_list|,
literal|2
argument_list|)
expr_stmt|;
name|this
operator|.
name|conf
operator|.
name|setInt
argument_list|(
name|CompactionConfiguration
operator|.
name|HBASE_HSTORE_COMPACTION_MAX_KEY
argument_list|,
literal|12
argument_list|)
expr_stmt|;
name|this
operator|.
name|conf
operator|.
name|setFloat
argument_list|(
name|CompactionConfiguration
operator|.
name|HBASE_HSTORE_COMPACTION_RATIO_KEY
argument_list|,
literal|1.2F
argument_list|)
expr_stmt|;
block|}
name|void
name|compactEquals
parameter_list|(
name|long
name|now
parameter_list|,
name|ArrayList
argument_list|<
name|StoreFile
argument_list|>
name|candidates
parameter_list|,
name|long
modifier|...
name|expected
parameter_list|)
throws|throws
name|IOException
block|{
name|Assert
operator|.
name|assertTrue
argument_list|(
operator|(
operator|(
name|DateTieredCompactionPolicy
operator|)
name|store
operator|.
name|storeEngine
operator|.
name|getCompactionPolicy
argument_list|()
operator|)
operator|.
name|needsCompaction
argument_list|(
name|candidates
argument_list|,
name|ImmutableList
operator|.
expr|<
name|StoreFile
operator|>
name|of
argument_list|()
argument_list|,
name|now
argument_list|)
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|StoreFile
argument_list|>
name|actual
init|=
operator|(
operator|(
name|DateTieredCompactionPolicy
operator|)
name|store
operator|.
name|storeEngine
operator|.
name|getCompactionPolicy
argument_list|()
operator|)
operator|.
name|applyCompactionPolicy
argument_list|(
name|candidates
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|,
name|now
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|Arrays
operator|.
name|toString
argument_list|(
name|expected
argument_list|)
argument_list|,
name|Arrays
operator|.
name|toString
argument_list|(
name|getSizes
argument_list|(
name|actual
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test for incoming window    * @throws IOException with error    */
annotation|@
name|Test
specifier|public
name|void
name|incomingWindow
parameter_list|()
throws|throws
name|IOException
block|{
name|long
index|[]
name|minTimestamps
init|=
operator|new
name|long
index|[]
block|{
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|}
decl_stmt|;
name|long
index|[]
name|maxTimestamps
init|=
operator|new
name|long
index|[]
block|{
literal|1
block|,
literal|2
block|,
literal|3
block|,
literal|4
block|,
literal|5
block|,
literal|6
block|,
literal|7
block|,
literal|8
block|,
literal|9
block|,
literal|10
block|,
literal|11
block|,
literal|12
block|,
literal|13
block|,
literal|14
block|,
literal|15
block|}
decl_stmt|;
name|long
index|[]
name|sizes
init|=
operator|new
name|long
index|[]
block|{
literal|30
block|,
literal|31
block|,
literal|32
block|,
literal|33
block|,
literal|34
block|,
literal|20
block|,
literal|21
block|,
literal|22
block|,
literal|23
block|,
literal|24
block|,
literal|25
block|,
literal|10
block|,
literal|11
block|,
literal|12
block|,
literal|13
block|}
decl_stmt|;
name|compactEquals
argument_list|(
literal|16
argument_list|,
name|sfCreate
argument_list|(
name|minTimestamps
argument_list|,
name|maxTimestamps
argument_list|,
name|sizes
argument_list|)
argument_list|,
literal|13
argument_list|,
literal|12
argument_list|,
literal|11
argument_list|,
literal|10
argument_list|)
expr_stmt|;
block|}
comment|/**    * Not enough files in incoming window    * @throws IOException with error    */
annotation|@
name|Test
specifier|public
name|void
name|NotIncomingWindow
parameter_list|()
throws|throws
name|IOException
block|{
name|long
index|[]
name|minTimestamps
init|=
operator|new
name|long
index|[]
block|{
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|}
decl_stmt|;
name|long
index|[]
name|maxTimestamps
init|=
operator|new
name|long
index|[]
block|{
literal|1
block|,
literal|2
block|,
literal|3
block|,
literal|4
block|,
literal|5
block|,
literal|6
block|,
literal|7
block|,
literal|8
block|,
literal|9
block|,
literal|10
block|,
literal|11
block|,
literal|12
block|,
literal|13
block|}
decl_stmt|;
name|long
index|[]
name|sizes
init|=
operator|new
name|long
index|[]
block|{
literal|30
block|,
literal|31
block|,
literal|32
block|,
literal|33
block|,
literal|34
block|,
literal|20
block|,
literal|21
block|,
literal|22
block|,
literal|23
block|,
literal|24
block|,
literal|25
block|,
literal|10
block|,
literal|11
block|}
decl_stmt|;
name|compactEquals
argument_list|(
literal|16
argument_list|,
name|sfCreate
argument_list|(
name|minTimestamps
argument_list|,
name|maxTimestamps
argument_list|,
name|sizes
argument_list|)
argument_list|,
literal|25
argument_list|,
literal|24
argument_list|,
literal|23
argument_list|,
literal|22
argument_list|,
literal|21
argument_list|,
literal|20
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test for file newer than incoming window    * @throws IOException with error    */
annotation|@
name|Test
specifier|public
name|void
name|NewerThanIncomingWindow
parameter_list|()
throws|throws
name|IOException
block|{
name|long
index|[]
name|minTimestamps
init|=
operator|new
name|long
index|[]
block|{
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|}
decl_stmt|;
name|long
index|[]
name|maxTimestamps
init|=
operator|new
name|long
index|[]
block|{
literal|1
block|,
literal|2
block|,
literal|3
block|,
literal|4
block|,
literal|5
block|,
literal|6
block|,
literal|7
block|,
literal|8
block|,
literal|9
block|,
literal|10
block|,
literal|11
block|,
literal|12
block|,
literal|13
block|,
literal|14
block|,
literal|18
block|}
decl_stmt|;
name|long
index|[]
name|sizes
init|=
operator|new
name|long
index|[]
block|{
literal|30
block|,
literal|31
block|,
literal|32
block|,
literal|33
block|,
literal|34
block|,
literal|20
block|,
literal|21
block|,
literal|22
block|,
literal|23
block|,
literal|24
block|,
literal|25
block|,
literal|10
block|,
literal|11
block|,
literal|12
block|,
literal|13
block|}
decl_stmt|;
name|compactEquals
argument_list|(
literal|16
argument_list|,
name|sfCreate
argument_list|(
name|minTimestamps
argument_list|,
name|maxTimestamps
argument_list|,
name|sizes
argument_list|)
argument_list|,
literal|13
argument_list|,
literal|12
argument_list|,
literal|11
argument_list|,
literal|10
argument_list|)
expr_stmt|;
block|}
comment|/**    * If there is no T1 window, we don't build 2    * @throws IOException with error    */
annotation|@
name|Test
specifier|public
name|void
name|NoT2
parameter_list|()
throws|throws
name|IOException
block|{
name|long
index|[]
name|minTimestamps
init|=
operator|new
name|long
index|[]
block|{
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|}
decl_stmt|;
name|long
index|[]
name|maxTimestamps
init|=
operator|new
name|long
index|[]
block|{
literal|44
block|,
literal|60
block|,
literal|61
block|,
literal|92
block|,
literal|95
block|,
literal|100
block|}
decl_stmt|;
name|long
index|[]
name|sizes
init|=
operator|new
name|long
index|[]
block|{
literal|0
block|,
literal|20
block|,
literal|21
block|,
literal|22
block|,
literal|23
block|,
literal|1
block|}
decl_stmt|;
name|compactEquals
argument_list|(
literal|100
argument_list|,
name|sfCreate
argument_list|(
name|minTimestamps
argument_list|,
name|maxTimestamps
argument_list|,
name|sizes
argument_list|)
argument_list|,
literal|23
argument_list|,
literal|22
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|T1
parameter_list|()
throws|throws
name|IOException
block|{
name|long
index|[]
name|minTimestamps
init|=
operator|new
name|long
index|[]
block|{
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|}
decl_stmt|;
name|long
index|[]
name|maxTimestamps
init|=
operator|new
name|long
index|[]
block|{
literal|44
block|,
literal|60
block|,
literal|61
block|,
literal|96
block|,
literal|100
block|,
literal|104
block|,
literal|120
block|,
literal|124
block|,
literal|143
block|,
literal|145
block|,
literal|157
block|}
decl_stmt|;
name|long
index|[]
name|sizes
init|=
operator|new
name|long
index|[]
block|{
literal|0
block|,
literal|50
block|,
literal|51
block|,
literal|40
block|,
literal|41
block|,
literal|42
block|,
literal|30
block|,
literal|31
block|,
literal|32
block|,
literal|2
block|,
literal|1
block|}
decl_stmt|;
name|compactEquals
argument_list|(
literal|161
argument_list|,
name|sfCreate
argument_list|(
name|minTimestamps
argument_list|,
name|maxTimestamps
argument_list|,
name|sizes
argument_list|)
argument_list|,
literal|32
argument_list|,
literal|31
argument_list|,
literal|30
argument_list|)
expr_stmt|;
block|}
comment|/**    * Apply exploring logic on non-incoming window    * @throws IOException with error    */
annotation|@
name|Test
specifier|public
name|void
name|RatioT0
parameter_list|()
throws|throws
name|IOException
block|{
name|long
index|[]
name|minTimestamps
init|=
operator|new
name|long
index|[]
block|{
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|}
decl_stmt|;
name|long
index|[]
name|maxTimestamps
init|=
operator|new
name|long
index|[]
block|{
literal|1
block|,
literal|2
block|,
literal|3
block|,
literal|4
block|,
literal|5
block|,
literal|6
block|,
literal|7
block|,
literal|8
block|,
literal|9
block|,
literal|10
block|,
literal|11
block|,
literal|12
block|}
decl_stmt|;
name|long
index|[]
name|sizes
init|=
operator|new
name|long
index|[]
block|{
literal|30
block|,
literal|31
block|,
literal|32
block|,
literal|33
block|,
literal|34
block|,
literal|20
block|,
literal|21
block|,
literal|22
block|,
literal|280
block|,
literal|23
block|,
literal|24
block|,
literal|1
block|}
decl_stmt|;
name|compactEquals
argument_list|(
literal|16
argument_list|,
name|sfCreate
argument_list|(
name|minTimestamps
argument_list|,
name|maxTimestamps
argument_list|,
name|sizes
argument_list|)
argument_list|,
literal|22
argument_list|,
literal|21
argument_list|,
literal|20
argument_list|)
expr_stmt|;
block|}
comment|/**    * Also apply ratio-based logic on t2 window    * @throws IOException with error    */
annotation|@
name|Test
specifier|public
name|void
name|RatioT2
parameter_list|()
throws|throws
name|IOException
block|{
name|long
index|[]
name|minTimestamps
init|=
operator|new
name|long
index|[]
block|{
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|}
decl_stmt|;
name|long
index|[]
name|maxTimestamps
init|=
operator|new
name|long
index|[]
block|{
literal|44
block|,
literal|60
block|,
literal|61
block|,
literal|96
block|,
literal|100
block|,
literal|104
block|,
literal|120
block|,
literal|124
block|,
literal|143
block|,
literal|145
block|,
literal|157
block|}
decl_stmt|;
name|long
index|[]
name|sizes
init|=
operator|new
name|long
index|[]
block|{
literal|0
block|,
literal|50
block|,
literal|51
block|,
literal|40
block|,
literal|41
block|,
literal|42
block|,
literal|350
block|,
literal|30
block|,
literal|31
block|,
literal|2
block|,
literal|1
block|}
decl_stmt|;
name|compactEquals
argument_list|(
literal|161
argument_list|,
name|sfCreate
argument_list|(
name|minTimestamps
argument_list|,
name|maxTimestamps
argument_list|,
name|sizes
argument_list|)
argument_list|,
literal|31
argument_list|,
literal|30
argument_list|)
expr_stmt|;
block|}
comment|/**    * The next compaction call after testTieredCompactionRatioT0 is compacted    * @throws IOException with error    */
annotation|@
name|Test
specifier|public
name|void
name|RatioT0Next
parameter_list|()
throws|throws
name|IOException
block|{
name|long
index|[]
name|minTimestamps
init|=
operator|new
name|long
index|[]
block|{
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|}
decl_stmt|;
name|long
index|[]
name|maxTimestamps
init|=
operator|new
name|long
index|[]
block|{
literal|1
block|,
literal|2
block|,
literal|3
block|,
literal|4
block|,
literal|5
block|,
literal|8
block|,
literal|9
block|,
literal|10
block|,
literal|11
block|,
literal|12
block|}
decl_stmt|;
name|long
index|[]
name|sizes
init|=
operator|new
name|long
index|[]
block|{
literal|30
block|,
literal|31
block|,
literal|32
block|,
literal|33
block|,
literal|34
block|,
literal|22
block|,
literal|280
block|,
literal|23
block|,
literal|24
block|,
literal|1
block|}
decl_stmt|;
name|compactEquals
argument_list|(
literal|16
argument_list|,
name|sfCreate
argument_list|(
name|minTimestamps
argument_list|,
name|maxTimestamps
argument_list|,
name|sizes
argument_list|)
argument_list|,
literal|24
argument_list|,
literal|23
argument_list|)
expr_stmt|;
block|}
comment|/**    * Older than now(161) - maxAge(100)    * @throws IOException with error    */
annotation|@
name|Test
specifier|public
name|void
name|olderThanMaxAge
parameter_list|()
throws|throws
name|IOException
block|{
name|long
index|[]
name|minTimestamps
init|=
operator|new
name|long
index|[]
block|{
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|}
decl_stmt|;
name|long
index|[]
name|maxTimestamps
init|=
operator|new
name|long
index|[]
block|{
literal|44
block|,
literal|60
block|,
literal|61
block|,
literal|96
block|,
literal|100
block|,
literal|104
block|,
literal|105
block|,
literal|106
block|,
literal|113
block|,
literal|145
block|,
literal|157
block|}
decl_stmt|;
name|long
index|[]
name|sizes
init|=
operator|new
name|long
index|[]
block|{
literal|0
block|,
literal|50
block|,
literal|51
block|,
literal|40
block|,
literal|41
block|,
literal|42
block|,
literal|33
block|,
literal|30
block|,
literal|31
block|,
literal|2
block|,
literal|1
block|}
decl_stmt|;
name|compactEquals
argument_list|(
literal|161
argument_list|,
name|sfCreate
argument_list|(
name|minTimestamps
argument_list|,
name|maxTimestamps
argument_list|,
name|sizes
argument_list|)
argument_list|,
literal|31
argument_list|,
literal|30
argument_list|,
literal|33
argument_list|,
literal|42
argument_list|,
literal|41
argument_list|,
literal|40
argument_list|)
expr_stmt|;
block|}
comment|/**    * Out-of-order data    * @throws IOException with error    */
annotation|@
name|Test
specifier|public
name|void
name|OutOfOrder
parameter_list|()
throws|throws
name|IOException
block|{
name|long
index|[]
name|minTimestamps
init|=
operator|new
name|long
index|[]
block|{
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|,
literal|0
block|}
decl_stmt|;
name|long
index|[]
name|maxTimestamps
init|=
operator|new
name|long
index|[]
block|{
literal|0
block|,
literal|13
block|,
literal|3
block|,
literal|10
block|,
literal|11
block|,
literal|1
block|,
literal|2
block|,
literal|12
block|,
literal|14
block|,
literal|15
block|}
decl_stmt|;
name|long
index|[]
name|sizes
init|=
operator|new
name|long
index|[]
block|{
literal|30
block|,
literal|31
block|,
literal|32
block|,
literal|33
block|,
literal|34
block|,
literal|22
block|,
literal|28
block|,
literal|23
block|,
literal|24
block|,
literal|1
block|}
decl_stmt|;
name|compactEquals
argument_list|(
literal|16
argument_list|,
name|sfCreate
argument_list|(
name|minTimestamps
argument_list|,
name|maxTimestamps
argument_list|,
name|sizes
argument_list|)
argument_list|,
literal|1
argument_list|,
literal|24
argument_list|,
literal|23
argument_list|,
literal|28
argument_list|,
literal|22
argument_list|,
literal|34
argument_list|,
literal|33
argument_list|,
literal|32
argument_list|,
literal|31
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

