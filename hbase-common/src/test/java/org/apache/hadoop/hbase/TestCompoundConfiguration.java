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
name|assertNull
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
name|fail
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
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
name|testclassification
operator|.
name|MiscTests
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
name|junit
operator|.
name|Before
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
name|MiscTests
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
name|TestCompoundConfiguration
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
name|TestCompoundConfiguration
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|Configuration
name|baseConf
decl_stmt|;
specifier|private
name|int
name|baseConfSize
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|baseConf
operator|=
operator|new
name|Configuration
argument_list|()
expr_stmt|;
name|baseConf
operator|.
name|set
argument_list|(
literal|"A"
argument_list|,
literal|"1"
argument_list|)
expr_stmt|;
name|baseConf
operator|.
name|setInt
argument_list|(
literal|"B"
argument_list|,
literal|2
argument_list|)
expr_stmt|;
name|baseConf
operator|.
name|set
argument_list|(
literal|"C"
argument_list|,
literal|"3"
argument_list|)
expr_stmt|;
name|baseConfSize
operator|=
name|baseConf
operator|.
name|size
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testBasicFunctionality
parameter_list|()
throws|throws
name|ClassNotFoundException
block|{
name|CompoundConfiguration
name|compoundConf
init|=
operator|new
name|CompoundConfiguration
argument_list|()
operator|.
name|add
argument_list|(
name|baseConf
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"1"
argument_list|,
name|compoundConf
operator|.
name|get
argument_list|(
literal|"A"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|compoundConf
operator|.
name|getInt
argument_list|(
literal|"B"
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|compoundConf
operator|.
name|getInt
argument_list|(
literal|"C"
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|compoundConf
operator|.
name|getInt
argument_list|(
literal|"D"
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|CompoundConfiguration
operator|.
name|class
argument_list|,
name|compoundConf
operator|.
name|getClassByName
argument_list|(
name|CompoundConfiguration
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
try|try
block|{
name|compoundConf
operator|.
name|getClassByName
argument_list|(
literal|"bad_class_name"
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"Trying to load bad_class_name should throw an exception"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ClassNotFoundException
name|e
parameter_list|)
block|{
comment|// win!
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testPut
parameter_list|()
block|{
name|CompoundConfiguration
name|compoundConf
init|=
operator|new
name|CompoundConfiguration
argument_list|()
operator|.
name|add
argument_list|(
name|baseConf
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"1"
argument_list|,
name|compoundConf
operator|.
name|get
argument_list|(
literal|"A"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|compoundConf
operator|.
name|getInt
argument_list|(
literal|"B"
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|compoundConf
operator|.
name|getInt
argument_list|(
literal|"C"
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|compoundConf
operator|.
name|getInt
argument_list|(
literal|"D"
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|compoundConf
operator|.
name|set
argument_list|(
literal|"A"
argument_list|,
literal|"1337"
argument_list|)
expr_stmt|;
name|compoundConf
operator|.
name|set
argument_list|(
literal|"string"
argument_list|,
literal|"stringvalue"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1337
argument_list|,
name|compoundConf
operator|.
name|getInt
argument_list|(
literal|"A"
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"stringvalue"
argument_list|,
name|compoundConf
operator|.
name|get
argument_list|(
literal|"string"
argument_list|)
argument_list|)
expr_stmt|;
comment|// we didn't modify the base conf
name|assertEquals
argument_list|(
literal|"1"
argument_list|,
name|baseConf
operator|.
name|get
argument_list|(
literal|"A"
argument_list|)
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|baseConf
operator|.
name|get
argument_list|(
literal|"string"
argument_list|)
argument_list|)
expr_stmt|;
comment|// adding to the base shows up in the compound
name|baseConf
operator|.
name|set
argument_list|(
literal|"setInParent"
argument_list|,
literal|"fromParent"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"fromParent"
argument_list|,
name|compoundConf
operator|.
name|get
argument_list|(
literal|"setInParent"
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testWithConfig
parameter_list|()
block|{
name|Configuration
name|conf
init|=
operator|new
name|Configuration
argument_list|()
decl_stmt|;
name|conf
operator|.
name|set
argument_list|(
literal|"B"
argument_list|,
literal|"2b"
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
literal|"C"
argument_list|,
literal|"33"
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
literal|"D"
argument_list|,
literal|"4"
argument_list|)
expr_stmt|;
name|CompoundConfiguration
name|compoundConf
init|=
operator|new
name|CompoundConfiguration
argument_list|()
operator|.
name|add
argument_list|(
name|baseConf
argument_list|)
operator|.
name|add
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"1"
argument_list|,
name|compoundConf
operator|.
name|get
argument_list|(
literal|"A"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"2b"
argument_list|,
name|compoundConf
operator|.
name|get
argument_list|(
literal|"B"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|33
argument_list|,
name|compoundConf
operator|.
name|getInt
argument_list|(
literal|"C"
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"4"
argument_list|,
name|compoundConf
operator|.
name|get
argument_list|(
literal|"D"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|4
argument_list|,
name|compoundConf
operator|.
name|getInt
argument_list|(
literal|"D"
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|compoundConf
operator|.
name|get
argument_list|(
literal|"E"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|6
argument_list|,
name|compoundConf
operator|.
name|getInt
argument_list|(
literal|"F"
argument_list|,
literal|6
argument_list|)
argument_list|)
expr_stmt|;
name|int
name|cnt
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|entry
range|:
name|compoundConf
control|)
block|{
name|cnt
operator|++
expr_stmt|;
if|if
condition|(
name|entry
operator|.
name|getKey
argument_list|()
operator|.
name|equals
argument_list|(
literal|"B"
argument_list|)
condition|)
block|{
name|assertEquals
argument_list|(
literal|"2b"
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|entry
operator|.
name|getKey
argument_list|()
operator|.
name|equals
argument_list|(
literal|"G"
argument_list|)
condition|)
block|{
name|assertNull
argument_list|(
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|// verify that entries from ImmutableConfigMap's are merged in the iterator's view
name|assertEquals
argument_list|(
name|baseConfSize
operator|+
literal|1
argument_list|,
name|cnt
argument_list|)
expr_stmt|;
block|}
specifier|private
name|Bytes
name|strToIb
parameter_list|(
name|String
name|s
parameter_list|)
block|{
return|return
operator|new
name|Bytes
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|s
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testWithIbwMap
parameter_list|()
block|{
name|Map
argument_list|<
name|Bytes
argument_list|,
name|Bytes
argument_list|>
name|map
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|map
operator|.
name|put
argument_list|(
name|strToIb
argument_list|(
literal|"B"
argument_list|)
argument_list|,
name|strToIb
argument_list|(
literal|"2b"
argument_list|)
argument_list|)
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
name|strToIb
argument_list|(
literal|"C"
argument_list|)
argument_list|,
name|strToIb
argument_list|(
literal|"33"
argument_list|)
argument_list|)
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
name|strToIb
argument_list|(
literal|"D"
argument_list|)
argument_list|,
name|strToIb
argument_list|(
literal|"4"
argument_list|)
argument_list|)
expr_stmt|;
comment|// unlike config, note that IBW Maps can accept null values
name|map
operator|.
name|put
argument_list|(
name|strToIb
argument_list|(
literal|"G"
argument_list|)
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|CompoundConfiguration
name|compoundConf
init|=
operator|new
name|CompoundConfiguration
argument_list|()
operator|.
name|add
argument_list|(
name|baseConf
argument_list|)
operator|.
name|addBytesMap
argument_list|(
name|map
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"1"
argument_list|,
name|compoundConf
operator|.
name|get
argument_list|(
literal|"A"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"2b"
argument_list|,
name|compoundConf
operator|.
name|get
argument_list|(
literal|"B"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|33
argument_list|,
name|compoundConf
operator|.
name|getInt
argument_list|(
literal|"C"
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"4"
argument_list|,
name|compoundConf
operator|.
name|get
argument_list|(
literal|"D"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|4
argument_list|,
name|compoundConf
operator|.
name|getInt
argument_list|(
literal|"D"
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|compoundConf
operator|.
name|get
argument_list|(
literal|"E"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|6
argument_list|,
name|compoundConf
operator|.
name|getInt
argument_list|(
literal|"F"
argument_list|,
literal|6
argument_list|)
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|compoundConf
operator|.
name|get
argument_list|(
literal|"G"
argument_list|)
argument_list|)
expr_stmt|;
name|int
name|cnt
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|entry
range|:
name|compoundConf
control|)
block|{
name|cnt
operator|++
expr_stmt|;
if|if
condition|(
name|entry
operator|.
name|getKey
argument_list|()
operator|.
name|equals
argument_list|(
literal|"B"
argument_list|)
condition|)
block|{
name|assertEquals
argument_list|(
literal|"2b"
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|entry
operator|.
name|getKey
argument_list|()
operator|.
name|equals
argument_list|(
literal|"G"
argument_list|)
condition|)
block|{
name|assertNull
argument_list|(
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|// verify that entries from ImmutableConfigMap's are merged in the iterator's view
name|assertEquals
argument_list|(
name|baseConfSize
operator|+
literal|2
argument_list|,
name|cnt
argument_list|)
expr_stmt|;
comment|// Verify that adding map after compound configuration is modified overrides properly
name|CompoundConfiguration
name|conf2
init|=
operator|new
name|CompoundConfiguration
argument_list|()
decl_stmt|;
name|conf2
operator|.
name|set
argument_list|(
literal|"X"
argument_list|,
literal|"modification"
argument_list|)
expr_stmt|;
name|conf2
operator|.
name|set
argument_list|(
literal|"D"
argument_list|,
literal|"not4"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"modification"
argument_list|,
name|conf2
operator|.
name|get
argument_list|(
literal|"X"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"not4"
argument_list|,
name|conf2
operator|.
name|get
argument_list|(
literal|"D"
argument_list|)
argument_list|)
expr_stmt|;
name|conf2
operator|.
name|addBytesMap
argument_list|(
name|map
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"4"
argument_list|,
name|conf2
operator|.
name|get
argument_list|(
literal|"D"
argument_list|)
argument_list|)
expr_stmt|;
comment|// map overrides
block|}
annotation|@
name|Test
specifier|public
name|void
name|testWithStringMap
parameter_list|()
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|map
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|map
operator|.
name|put
argument_list|(
literal|"B"
argument_list|,
literal|"2b"
argument_list|)
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
literal|"C"
argument_list|,
literal|"33"
argument_list|)
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
literal|"D"
argument_list|,
literal|"4"
argument_list|)
expr_stmt|;
comment|// unlike config, note that IBW Maps can accept null values
name|map
operator|.
name|put
argument_list|(
literal|"G"
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|CompoundConfiguration
name|compoundConf
init|=
operator|new
name|CompoundConfiguration
argument_list|()
operator|.
name|addStringMap
argument_list|(
name|map
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"2b"
argument_list|,
name|compoundConf
operator|.
name|get
argument_list|(
literal|"B"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|33
argument_list|,
name|compoundConf
operator|.
name|getInt
argument_list|(
literal|"C"
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"4"
argument_list|,
name|compoundConf
operator|.
name|get
argument_list|(
literal|"D"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|4
argument_list|,
name|compoundConf
operator|.
name|getInt
argument_list|(
literal|"D"
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|compoundConf
operator|.
name|get
argument_list|(
literal|"E"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|6
argument_list|,
name|compoundConf
operator|.
name|getInt
argument_list|(
literal|"F"
argument_list|,
literal|6
argument_list|)
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|compoundConf
operator|.
name|get
argument_list|(
literal|"G"
argument_list|)
argument_list|)
expr_stmt|;
name|int
name|cnt
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|entry
range|:
name|compoundConf
control|)
block|{
name|cnt
operator|++
expr_stmt|;
if|if
condition|(
name|entry
operator|.
name|getKey
argument_list|()
operator|.
name|equals
argument_list|(
literal|"B"
argument_list|)
condition|)
block|{
name|assertEquals
argument_list|(
literal|"2b"
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|entry
operator|.
name|getKey
argument_list|()
operator|.
name|equals
argument_list|(
literal|"G"
argument_list|)
condition|)
block|{
name|assertNull
argument_list|(
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|// verify that entries from ImmutableConfigMap's are merged in the iterator's view
name|assertEquals
argument_list|(
literal|4
argument_list|,
name|cnt
argument_list|)
expr_stmt|;
comment|// Verify that adding map after compound configuration is modified overrides properly
name|CompoundConfiguration
name|conf2
init|=
operator|new
name|CompoundConfiguration
argument_list|()
decl_stmt|;
name|conf2
operator|.
name|set
argument_list|(
literal|"X"
argument_list|,
literal|"modification"
argument_list|)
expr_stmt|;
name|conf2
operator|.
name|set
argument_list|(
literal|"D"
argument_list|,
literal|"not4"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"modification"
argument_list|,
name|conf2
operator|.
name|get
argument_list|(
literal|"X"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"not4"
argument_list|,
name|conf2
operator|.
name|get
argument_list|(
literal|"D"
argument_list|)
argument_list|)
expr_stmt|;
name|conf2
operator|.
name|addStringMap
argument_list|(
name|map
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"4"
argument_list|,
name|conf2
operator|.
name|get
argument_list|(
literal|"D"
argument_list|)
argument_list|)
expr_stmt|;
comment|// map overrides
block|}
annotation|@
name|Test
specifier|public
name|void
name|testLaterConfigsOverrideEarlier
parameter_list|()
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|map1
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|map1
operator|.
name|put
argument_list|(
literal|"A"
argument_list|,
literal|"2"
argument_list|)
expr_stmt|;
name|map1
operator|.
name|put
argument_list|(
literal|"D"
argument_list|,
literal|"5"
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|map2
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|String
name|newValueForA
init|=
literal|"3"
decl_stmt|,
name|newValueForB
init|=
literal|"4"
decl_stmt|;
name|map2
operator|.
name|put
argument_list|(
literal|"A"
argument_list|,
name|newValueForA
argument_list|)
expr_stmt|;
name|map2
operator|.
name|put
argument_list|(
literal|"B"
argument_list|,
name|newValueForB
argument_list|)
expr_stmt|;
name|CompoundConfiguration
name|compoundConf
init|=
operator|new
name|CompoundConfiguration
argument_list|()
operator|.
name|addStringMap
argument_list|(
name|map1
argument_list|)
operator|.
name|add
argument_list|(
name|baseConf
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"1"
argument_list|,
name|compoundConf
operator|.
name|get
argument_list|(
literal|"A"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"5"
argument_list|,
name|compoundConf
operator|.
name|get
argument_list|(
literal|"D"
argument_list|)
argument_list|)
expr_stmt|;
name|compoundConf
operator|.
name|addStringMap
argument_list|(
name|map2
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|newValueForA
argument_list|,
name|compoundConf
operator|.
name|get
argument_list|(
literal|"A"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|newValueForB
argument_list|,
name|compoundConf
operator|.
name|get
argument_list|(
literal|"B"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"5"
argument_list|,
name|compoundConf
operator|.
name|get
argument_list|(
literal|"D"
argument_list|)
argument_list|)
expr_stmt|;
name|int
name|cnt
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|entry
range|:
name|compoundConf
control|)
block|{
name|cnt
operator|++
expr_stmt|;
if|if
condition|(
name|entry
operator|.
name|getKey
argument_list|()
operator|.
name|equals
argument_list|(
literal|"A"
argument_list|)
condition|)
block|{
name|assertEquals
argument_list|(
name|newValueForA
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|entry
operator|.
name|getKey
argument_list|()
operator|.
name|equals
argument_list|(
literal|"B"
argument_list|)
condition|)
block|{
name|assertEquals
argument_list|(
name|newValueForB
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|// verify that entries from ImmutableConfigMap's are merged in the iterator's view
name|assertEquals
argument_list|(
name|baseConfSize
operator|+
literal|1
argument_list|,
name|cnt
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

