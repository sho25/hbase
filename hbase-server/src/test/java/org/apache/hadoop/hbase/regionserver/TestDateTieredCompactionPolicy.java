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
name|regionserver
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
name|hadoop
operator|.
name|hbase
operator|.
name|HBaseClassTestRule
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
name|ExponentialCompactionWindowFactory
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
name|RegionServerTests
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
name|ClassRule
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
block|{
name|RegionServerTests
operator|.
name|class
block|,
name|SmallTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestDateTieredCompactionPolicy
extends|extends
name|AbstractTestDateTieredCompactionPolicy
block|{
annotation|@
name|ClassRule
specifier|public
specifier|static
specifier|final
name|HBaseClassTestRule
name|CLASS_RULE
init|=
name|HBaseClassTestRule
operator|.
name|forClass
argument_list|(
name|TestDateTieredCompactionPolicy
operator|.
name|class
argument_list|)
decl_stmt|;
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
name|set
argument_list|(
name|StoreEngine
operator|.
name|STORE_ENGINE_CLASS_KEY
argument_list|,
literal|"org.apache.hadoop.hbase.regionserver.DateTieredStoreEngine"
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setLong
argument_list|(
name|CompactionConfiguration
operator|.
name|DATE_TIERED_MAX_AGE_MILLIS_KEY
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
name|DATE_TIERED_INCOMING_WINDOW_MIN_KEY
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setLong
argument_list|(
name|ExponentialCompactionWindowFactory
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
name|ExponentialCompactionWindowFactory
operator|.
name|WINDOWS_PER_TIER_KEY
argument_list|,
literal|4
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setBoolean
argument_list|(
name|CompactionConfiguration
operator|.
name|DATE_TIERED_SINGLE_OUTPUT_FOR_MINOR_COMPACTION_KEY
argument_list|,
literal|false
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
name|conf
operator|.
name|setInt
argument_list|(
name|HStore
operator|.
name|BLOCKING_STOREFILES_KEY
argument_list|,
literal|20
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setLong
argument_list|(
name|HConstants
operator|.
name|MAJOR_COMPACTION_PERIOD
argument_list|,
literal|5
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
operator|new
name|long
index|[]
block|{
literal|10
block|,
literal|11
block|,
literal|12
block|,
literal|13
block|}
argument_list|,
operator|new
name|long
index|[]
block|{
name|Long
operator|.
name|MIN_VALUE
block|,
literal|12
block|}
argument_list|,
literal|false
argument_list|,
literal|true
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
operator|new
name|long
index|[]
block|{
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
block|}
argument_list|,
operator|new
name|long
index|[]
block|{
name|Long
operator|.
name|MIN_VALUE
block|,
literal|6
block|}
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test for file on the upper bound of incoming window    * @throws IOException with error    */
annotation|@
name|Test
specifier|public
name|void
name|OnUpperBoundOfIncomingWindow
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
operator|new
name|long
index|[]
block|{
literal|10
block|,
literal|11
block|,
literal|12
block|,
literal|13
block|}
argument_list|,
operator|new
name|long
index|[]
block|{
name|Long
operator|.
name|MIN_VALUE
block|,
literal|12
block|}
argument_list|,
literal|false
argument_list|,
literal|true
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
literal|19
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
operator|new
name|long
index|[]
block|{
literal|10
block|,
literal|11
block|,
literal|12
block|,
literal|13
block|}
argument_list|,
operator|new
name|long
index|[]
block|{
name|Long
operator|.
name|MIN_VALUE
block|,
literal|12
block|}
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
comment|/**    * If there is no T1 window, we don't build T2    * @throws IOException with error    */
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
literal|97
block|,
literal|100
block|,
literal|193
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
literal|194
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
operator|new
name|long
index|[]
block|{
literal|22
block|,
literal|23
block|}
argument_list|,
operator|new
name|long
index|[]
block|{
name|Long
operator|.
name|MIN_VALUE
block|,
literal|96
block|}
argument_list|,
literal|false
argument_list|,
literal|true
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
operator|new
name|long
index|[]
block|{
literal|30
block|,
literal|31
block|,
literal|32
block|}
argument_list|,
operator|new
name|long
index|[]
block|{
name|Long
operator|.
name|MIN_VALUE
block|,
literal|120
block|}
argument_list|,
literal|false
argument_list|,
literal|true
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
operator|new
name|long
index|[]
block|{
literal|20
block|,
literal|21
block|,
literal|22
block|}
argument_list|,
operator|new
name|long
index|[]
block|{
name|Long
operator|.
name|MIN_VALUE
block|}
argument_list|,
literal|false
argument_list|,
literal|true
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
operator|new
name|long
index|[]
block|{
literal|30
block|,
literal|31
block|}
argument_list|,
operator|new
name|long
index|[]
block|{
name|Long
operator|.
name|MIN_VALUE
block|}
argument_list|,
literal|false
argument_list|,
literal|true
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
operator|new
name|long
index|[]
block|{
literal|23
block|,
literal|24
block|}
argument_list|,
operator|new
name|long
index|[]
block|{
name|Long
operator|.
name|MIN_VALUE
block|}
argument_list|,
literal|false
argument_list|,
literal|true
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
operator|new
name|long
index|[]
block|{
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
block|}
argument_list|,
operator|new
name|long
index|[]
block|{
name|Long
operator|.
name|MIN_VALUE
block|,
literal|96
block|}
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
comment|/**    * Out-of-order data    * @throws IOException with error    */
annotation|@
name|Test
specifier|public
name|void
name|outOfOrder
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
operator|new
name|long
index|[]
block|{
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
argument_list|,
operator|new
name|long
index|[]
block|{
name|Long
operator|.
name|MIN_VALUE
block|,
literal|12
block|}
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
comment|/**    * Negative epoch time    * @throws IOException with error    */
annotation|@
name|Test
specifier|public
name|void
name|negativeEpochtime
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
operator|-
literal|1000
block|,
operator|-
literal|1000
block|,
operator|-
literal|1000
block|,
operator|-
literal|1000
block|,
operator|-
literal|1000
block|,
operator|-
literal|1000
block|,
operator|-
literal|1000
block|,
operator|-
literal|1000
block|,
operator|-
literal|1000
block|,
operator|-
literal|1000
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
operator|-
literal|28
block|,
operator|-
literal|11
block|,
operator|-
literal|10
block|,
operator|-
literal|9
block|,
operator|-
literal|8
block|,
operator|-
literal|7
block|,
operator|-
literal|6
block|,
operator|-
literal|5
block|,
operator|-
literal|4
block|,
operator|-
literal|3
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
literal|25
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
literal|1
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
operator|new
name|long
index|[]
block|{
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
literal|25
block|,
literal|23
block|,
literal|24
block|,
literal|1
block|}
argument_list|,
operator|new
name|long
index|[]
block|{
name|Long
operator|.
name|MIN_VALUE
block|,
operator|-
literal|24
block|}
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
comment|/**    * Major compaction    * @throws IOException with error    */
annotation|@
name|Test
specifier|public
name|void
name|majorCompation
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
argument_list|,
operator|new
name|long
index|[]
block|{
name|Long
operator|.
name|MIN_VALUE
block|,
literal|24
block|,
literal|48
block|,
literal|72
block|,
literal|96
block|,
literal|120
block|,
literal|144
block|,
literal|150
block|,
literal|156
block|}
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
comment|/**    * Major Compaction to check min max timestamp falling in the same window and also to check    * boundary condition in which case binary sort gives insertion point as length of the array    * @throws IOException    */
annotation|@
name|Test
specifier|public
name|void
name|checkMinMaxTimestampSameBoundary
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
literal|26
block|,
literal|50
block|,
literal|90
block|,
literal|98
block|,
literal|122
block|,
literal|145
block|,
literal|151
block|,
literal|158
block|,
literal|166
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
literal|12
block|,
literal|46
block|,
literal|70
block|,
literal|95
block|,
literal|100
block|,
literal|140
block|,
literal|148
block|,
literal|155
block|,
literal|162
block|,
literal|174
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
block|}
argument_list|,
operator|new
name|long
index|[]
block|{
name|Long
operator|.
name|MIN_VALUE
block|,
literal|24
block|,
literal|48
block|,
literal|72
block|,
literal|96
block|,
literal|120
block|,
literal|144
block|,
literal|150
block|,
literal|156
block|}
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
comment|/**    * Major compaction with negative numbers    * @throws IOException with error    */
annotation|@
name|Test
specifier|public
name|void
name|negativeForMajor
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
operator|-
literal|155
block|,
operator|-
literal|100
block|,
operator|-
literal|100
block|,
operator|-
literal|100
block|,
operator|-
literal|100
block|,
operator|-
literal|100
block|,
operator|-
literal|100
block|,
operator|-
literal|100
block|,
operator|-
literal|100
block|,
operator|-
literal|100
block|,
operator|-
literal|100
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
operator|-
literal|8
block|,
operator|-
literal|7
block|,
operator|-
literal|6
block|,
operator|-
literal|5
block|,
operator|-
literal|4
block|,
operator|-
literal|3
block|,
operator|-
literal|2
block|,
operator|-
literal|1
block|,
literal|0
block|,
literal|6
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
argument_list|,
operator|new
name|long
index|[]
block|{
name|Long
operator|.
name|MIN_VALUE
block|,
operator|-
literal|144
block|,
operator|-
literal|120
block|,
operator|-
literal|96
block|,
operator|-
literal|72
block|,
operator|-
literal|48
block|,
operator|-
literal|24
block|,
literal|0
block|,
literal|6
block|,
literal|12
block|}
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

