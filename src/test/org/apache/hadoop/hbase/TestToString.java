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
name|io
operator|.
name|Text
import|;
end_import

begin_import
import|import
name|junit
operator|.
name|framework
operator|.
name|TestCase
import|;
end_import

begin_comment
comment|/**  * Tests toString methods.  */
end_comment

begin_class
specifier|public
class|class
name|TestToString
extends|extends
name|TestCase
block|{
comment|/**    * tests toString methods on HSeverAddress, HServerInfo    * @throws Exception    */
specifier|public
name|void
name|testServerInfo
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|String
name|hostport
init|=
literal|"127.0.0.1:9999"
decl_stmt|;
name|HServerAddress
name|address
init|=
operator|new
name|HServerAddress
argument_list|(
name|hostport
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"HServerAddress toString"
argument_list|,
name|address
operator|.
name|toString
argument_list|()
argument_list|,
name|hostport
argument_list|)
expr_stmt|;
name|HServerInfo
name|info
init|=
operator|new
name|HServerInfo
argument_list|(
name|address
argument_list|,
operator|-
literal|1
argument_list|,
literal|60030
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"HServerInfo"
argument_list|,
literal|"address: "
operator|+
name|hostport
operator|+
literal|", startcode: -1"
operator|+
literal|", load: (requests: 0 regions: 0)"
argument_list|,
name|info
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Tests toString method on HRegionInfo    * @throws Exception    */
specifier|public
name|void
name|testHRegionInfo
parameter_list|()
throws|throws
name|Exception
block|{
name|HTableDescriptor
name|htd
init|=
operator|new
name|HTableDescriptor
argument_list|(
literal|"hank"
argument_list|)
decl_stmt|;
name|htd
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
literal|"hankfamily:"
argument_list|)
argument_list|)
expr_stmt|;
name|htd
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
operator|new
name|Text
argument_list|(
literal|"hankotherfamily:"
argument_list|)
argument_list|,
literal|10
argument_list|,
name|HColumnDescriptor
operator|.
name|CompressionType
operator|.
name|BLOCK
argument_list|,
literal|true
argument_list|,
literal|1000
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
name|htd
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Table descriptor"
argument_list|,
literal|"name: hank, families: "
operator|+
literal|"{hankfamily:={name: hankfamily, max versions: 3, compression: NONE, "
operator|+
literal|"in memory: false, max length: 2147483647, bloom filter: none}, "
operator|+
literal|"hankotherfamily:={name: hankotherfamily, max versions: 10, "
operator|+
literal|"compression: BLOCK, in memory: true, max length: 1000, "
operator|+
literal|"bloom filter: none}}"
argument_list|,
name|htd
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|HRegionInfo
name|hri
init|=
operator|new
name|HRegionInfo
argument_list|(
operator|-
literal|1
argument_list|,
name|htd
argument_list|,
operator|new
name|Text
argument_list|()
argument_list|,
operator|new
name|Text
argument_list|(
literal|"10"
argument_list|)
argument_list|)
decl_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
name|hri
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"HRegionInfo"
argument_list|,
literal|"regionname: hank,,-1, startKey:<>, tableDesc: {name: hank, "
operator|+
literal|"families: {hankfamily:={name: hankfamily, max versions: 3, "
operator|+
literal|"compression: NONE, in memory: false, max length: 2147483647, "
operator|+
literal|"bloom filter: none}, hankotherfamily:={name: hankotherfamily, "
operator|+
literal|"max versions: 10, compression: BLOCK, in memory: true, "
operator|+
literal|"max length: 1000, bloom filter: none}}}"
argument_list|,
name|hri
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

