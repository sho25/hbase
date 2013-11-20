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
package|;
end_package

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertEquals
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertFalse
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertNotSame
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertTrue
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
name|TestHRegionLocation
block|{
comment|/**    * HRegionLocations are equal if they have the same 'location' -- i.e. host and    * port -- even if they are carrying different regions.  Verify that is indeed    * the case.    */
annotation|@
name|Test
specifier|public
name|void
name|testHashAndEqualsCode
parameter_list|()
block|{
name|ServerName
name|hsa1
init|=
name|ServerName
operator|.
name|valueOf
argument_list|(
literal|"localhost"
argument_list|,
literal|1234
argument_list|,
operator|-
literal|1L
argument_list|)
decl_stmt|;
name|HRegionLocation
name|hrl1
init|=
operator|new
name|HRegionLocation
argument_list|(
name|HRegionInfo
operator|.
name|FIRST_META_REGIONINFO
argument_list|,
name|hsa1
argument_list|)
decl_stmt|;
name|HRegionLocation
name|hrl2
init|=
operator|new
name|HRegionLocation
argument_list|(
name|HRegionInfo
operator|.
name|FIRST_META_REGIONINFO
argument_list|,
name|hsa1
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|hrl1
operator|.
name|hashCode
argument_list|()
argument_list|,
name|hrl2
operator|.
name|hashCode
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|hrl1
operator|.
name|equals
argument_list|(
name|hrl2
argument_list|)
argument_list|)
expr_stmt|;
name|HRegionLocation
name|hrl3
init|=
operator|new
name|HRegionLocation
argument_list|(
name|HRegionInfo
operator|.
name|FIRST_META_REGIONINFO
argument_list|,
name|hsa1
argument_list|)
decl_stmt|;
name|assertNotSame
argument_list|(
name|hrl1
argument_list|,
name|hrl3
argument_list|)
expr_stmt|;
comment|// They are equal because they have same location even though they are
comment|// carrying different regions or timestamp.
name|assertTrue
argument_list|(
name|hrl1
operator|.
name|equals
argument_list|(
name|hrl3
argument_list|)
argument_list|)
expr_stmt|;
name|ServerName
name|hsa2
init|=
name|ServerName
operator|.
name|valueOf
argument_list|(
literal|"localhost"
argument_list|,
literal|12345
argument_list|,
operator|-
literal|1L
argument_list|)
decl_stmt|;
name|HRegionLocation
name|hrl4
init|=
operator|new
name|HRegionLocation
argument_list|(
name|HRegionInfo
operator|.
name|FIRST_META_REGIONINFO
argument_list|,
name|hsa2
argument_list|)
decl_stmt|;
comment|// These have same HRI but different locations so should be different.
name|assertFalse
argument_list|(
name|hrl3
operator|.
name|equals
argument_list|(
name|hrl4
argument_list|)
argument_list|)
expr_stmt|;
name|HRegionLocation
name|hrl5
init|=
operator|new
name|HRegionLocation
argument_list|(
name|hrl4
operator|.
name|getRegionInfo
argument_list|()
argument_list|,
name|hrl4
operator|.
name|getServerName
argument_list|()
argument_list|,
name|hrl4
operator|.
name|getSeqNum
argument_list|()
operator|+
literal|1
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|hrl4
operator|.
name|equals
argument_list|(
name|hrl5
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testToString
parameter_list|()
block|{
name|ServerName
name|hsa1
init|=
name|ServerName
operator|.
name|valueOf
argument_list|(
literal|"localhost"
argument_list|,
literal|1234
argument_list|,
operator|-
literal|1L
argument_list|)
decl_stmt|;
name|HRegionLocation
name|hrl1
init|=
operator|new
name|HRegionLocation
argument_list|(
name|HRegionInfo
operator|.
name|FIRST_META_REGIONINFO
argument_list|,
name|hsa1
argument_list|)
decl_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
name|hrl1
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCompareTo
parameter_list|()
block|{
name|ServerName
name|hsa1
init|=
name|ServerName
operator|.
name|valueOf
argument_list|(
literal|"localhost"
argument_list|,
literal|1234
argument_list|,
operator|-
literal|1L
argument_list|)
decl_stmt|;
name|HRegionLocation
name|hsl1
init|=
operator|new
name|HRegionLocation
argument_list|(
name|HRegionInfo
operator|.
name|FIRST_META_REGIONINFO
argument_list|,
name|hsa1
argument_list|)
decl_stmt|;
name|ServerName
name|hsa2
init|=
name|ServerName
operator|.
name|valueOf
argument_list|(
literal|"localhost"
argument_list|,
literal|1235
argument_list|,
operator|-
literal|1L
argument_list|)
decl_stmt|;
name|HRegionLocation
name|hsl2
init|=
operator|new
name|HRegionLocation
argument_list|(
name|HRegionInfo
operator|.
name|FIRST_META_REGIONINFO
argument_list|,
name|hsa2
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|hsl1
operator|.
name|compareTo
argument_list|(
name|hsl1
argument_list|)
operator|==
literal|0
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|hsl2
operator|.
name|compareTo
argument_list|(
name|hsl2
argument_list|)
operator|==
literal|0
argument_list|)
expr_stmt|;
name|int
name|compare1
init|=
name|hsl1
operator|.
name|compareTo
argument_list|(
name|hsl2
argument_list|)
decl_stmt|;
name|int
name|compare2
init|=
name|hsl2
operator|.
name|compareTo
argument_list|(
name|hsl1
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
operator|(
name|compare1
operator|>
literal|0
operator|)
condition|?
name|compare2
operator|<
literal|0
else|:
name|compare2
operator|>
literal|0
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

