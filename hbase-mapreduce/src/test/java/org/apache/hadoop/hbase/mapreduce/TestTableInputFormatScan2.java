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
name|mapreduce
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
name|testclassification
operator|.
name|LargeTests
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
name|VerySlowMapReduceTests
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

begin_comment
comment|/**  * TestTableInputFormatScan part 2.  * @see TestTableInputFormatScanBase  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|VerySlowMapReduceTests
operator|.
name|class
block|,
name|LargeTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestTableInputFormatScan2
extends|extends
name|TestTableInputFormatScanBase
block|{
comment|/**    * Tests a MR scan using specific start and stop rows.    *    * @throws IOException    * @throws ClassNotFoundException    * @throws InterruptedException    */
annotation|@
name|Test
specifier|public
name|void
name|testScanOBBToOPP
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
throws|,
name|ClassNotFoundException
block|{
name|testScan
argument_list|(
literal|"obb"
argument_list|,
literal|"opp"
argument_list|,
literal|"opo"
argument_list|)
expr_stmt|;
block|}
comment|/**    * Tests a MR scan using specific start and stop rows.    *    * @throws IOException    * @throws ClassNotFoundException    * @throws InterruptedException    */
annotation|@
name|Test
specifier|public
name|void
name|testScanOBBToQPP
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
throws|,
name|ClassNotFoundException
block|{
name|testScan
argument_list|(
literal|"obb"
argument_list|,
literal|"qpp"
argument_list|,
literal|"qpo"
argument_list|)
expr_stmt|;
block|}
comment|/**    * Tests a MR scan using specific start and stop rows.    *    * @throws IOException    * @throws ClassNotFoundException    * @throws InterruptedException    */
annotation|@
name|Test
specifier|public
name|void
name|testScanOPPToEmpty
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
throws|,
name|ClassNotFoundException
block|{
name|testScan
argument_list|(
literal|"opp"
argument_list|,
literal|null
argument_list|,
literal|"zzz"
argument_list|)
expr_stmt|;
block|}
comment|/**    * Tests a MR scan using specific start and stop rows.    *    * @throws IOException    * @throws ClassNotFoundException    * @throws InterruptedException    */
annotation|@
name|Test
specifier|public
name|void
name|testScanYYXToEmpty
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
throws|,
name|ClassNotFoundException
block|{
name|testScan
argument_list|(
literal|"yyx"
argument_list|,
literal|null
argument_list|,
literal|"zzz"
argument_list|)
expr_stmt|;
block|}
comment|/**    * Tests a MR scan using specific start and stop rows.    *    * @throws IOException    * @throws ClassNotFoundException    * @throws InterruptedException    */
annotation|@
name|Test
specifier|public
name|void
name|testScanYYYToEmpty
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
throws|,
name|ClassNotFoundException
block|{
name|testScan
argument_list|(
literal|"yyy"
argument_list|,
literal|null
argument_list|,
literal|"zzz"
argument_list|)
expr_stmt|;
block|}
comment|/**    * Tests a MR scan using specific start and stop rows.    *    * @throws IOException    * @throws ClassNotFoundException    * @throws InterruptedException    */
annotation|@
name|Test
specifier|public
name|void
name|testScanYZYToEmpty
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
throws|,
name|ClassNotFoundException
block|{
name|testScan
argument_list|(
literal|"yzy"
argument_list|,
literal|null
argument_list|,
literal|"zzz"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testScanFromConfiguration
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
throws|,
name|ClassNotFoundException
block|{
name|testScanFromConfiguration
argument_list|(
literal|"bba"
argument_list|,
literal|"bbd"
argument_list|,
literal|"bbc"
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit
