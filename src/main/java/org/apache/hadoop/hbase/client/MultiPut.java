begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Copyright 2010 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|client
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
name|HServerAddress
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
name|apache
operator|.
name|hadoop
operator|.
name|io
operator|.
name|Writable
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataInput
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataOutput
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
name|Collection
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
name|Iterator
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
name|Set
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
name|java
operator|.
name|util
operator|.
name|TreeSet
import|;
end_import

begin_comment
comment|/**  * @deprecated Use MultiAction instead  * Data type class for putting multiple regions worth of puts in one RPC.  */
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
name|MultiPut
extends|extends
name|Operation
implements|implements
name|Writable
block|{
specifier|public
name|HServerAddress
name|address
decl_stmt|;
comment|// client code ONLY
comment|// TODO make this configurable
specifier|public
specifier|static
specifier|final
name|int
name|DEFAULT_MAX_PUT_OUTPUT
init|=
literal|10
decl_stmt|;
comment|// map of regions to lists of puts for that region.
specifier|public
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|List
argument_list|<
name|Put
argument_list|>
argument_list|>
name|puts
init|=
operator|new
name|TreeMap
argument_list|<
name|byte
index|[]
argument_list|,
name|List
argument_list|<
name|Put
argument_list|>
argument_list|>
argument_list|(
name|Bytes
operator|.
name|BYTES_COMPARATOR
argument_list|)
decl_stmt|;
comment|/**    * Writable constructor only.    */
specifier|public
name|MultiPut
parameter_list|()
block|{}
comment|/**    * MultiPut for putting multiple regions worth of puts in one RPC.    * @param a address    */
specifier|public
name|MultiPut
parameter_list|(
name|HServerAddress
name|a
parameter_list|)
block|{
name|address
operator|=
name|a
expr_stmt|;
block|}
specifier|public
name|int
name|size
parameter_list|()
block|{
name|int
name|size
init|=
literal|0
decl_stmt|;
for|for
control|(
name|List
argument_list|<
name|Put
argument_list|>
name|l
range|:
name|puts
operator|.
name|values
argument_list|()
control|)
block|{
name|size
operator|+=
name|l
operator|.
name|size
argument_list|()
expr_stmt|;
block|}
return|return
name|size
return|;
block|}
specifier|public
name|void
name|add
parameter_list|(
name|byte
index|[]
name|regionName
parameter_list|,
name|Put
name|aPut
parameter_list|)
block|{
name|List
argument_list|<
name|Put
argument_list|>
name|rsput
init|=
name|puts
operator|.
name|get
argument_list|(
name|regionName
argument_list|)
decl_stmt|;
if|if
condition|(
name|rsput
operator|==
literal|null
condition|)
block|{
name|rsput
operator|=
operator|new
name|ArrayList
argument_list|<
name|Put
argument_list|>
argument_list|()
expr_stmt|;
name|puts
operator|.
name|put
argument_list|(
name|regionName
argument_list|,
name|rsput
argument_list|)
expr_stmt|;
block|}
name|rsput
operator|.
name|add
argument_list|(
name|aPut
argument_list|)
expr_stmt|;
block|}
specifier|public
name|Collection
argument_list|<
name|Put
argument_list|>
name|allPuts
parameter_list|()
block|{
name|List
argument_list|<
name|Put
argument_list|>
name|res
init|=
operator|new
name|ArrayList
argument_list|<
name|Put
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|List
argument_list|<
name|Put
argument_list|>
name|pp
range|:
name|puts
operator|.
name|values
argument_list|()
control|)
block|{
name|res
operator|.
name|addAll
argument_list|(
name|pp
argument_list|)
expr_stmt|;
block|}
return|return
name|res
return|;
block|}
comment|/**    * Compile the table and column family (i.e. schema) information     * into a String. Useful for parsing and aggregation by debugging,    * logging, and administration tools.    * @return Map    */
annotation|@
name|Override
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|getFingerprint
parameter_list|()
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|map
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
argument_list|()
decl_stmt|;
comment|// for extensibility, we have a map of table information that we will
comment|// populate with only family information for each table
name|Map
argument_list|<
name|String
argument_list|,
name|Map
argument_list|>
name|tableInfo
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|Map
argument_list|>
argument_list|()
decl_stmt|;
name|map
operator|.
name|put
argument_list|(
literal|"tables"
argument_list|,
name|tableInfo
argument_list|)
expr_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|byte
index|[]
argument_list|,
name|List
argument_list|<
name|Put
argument_list|>
argument_list|>
name|entry
range|:
name|puts
operator|.
name|entrySet
argument_list|()
control|)
block|{
comment|// our fingerprint only concerns itself with which families are touched,
comment|// not how many Puts touch them, so we use this Set to do just that.
name|Set
argument_list|<
name|String
argument_list|>
name|familySet
decl_stmt|;
try|try
block|{
comment|// since the puts are stored by region, we may have already
comment|// recorded families for this region. if that is the case,
comment|// we want to add to the existing Set. if not, we make a new Set.
name|String
name|tableName
init|=
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|HRegionInfo
operator|.
name|parseRegionName
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
index|[
literal|0
index|]
argument_list|)
decl_stmt|;
if|if
condition|(
name|tableInfo
operator|.
name|get
argument_list|(
name|tableName
argument_list|)
operator|==
literal|null
condition|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|table
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
argument_list|()
decl_stmt|;
name|familySet
operator|=
operator|new
name|TreeSet
argument_list|<
name|String
argument_list|>
argument_list|()
expr_stmt|;
name|table
operator|.
name|put
argument_list|(
literal|"families"
argument_list|,
name|familySet
argument_list|)
expr_stmt|;
name|tableInfo
operator|.
name|put
argument_list|(
name|tableName
argument_list|,
name|table
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|familySet
operator|=
operator|(
name|Set
argument_list|<
name|String
argument_list|>
operator|)
name|tableInfo
operator|.
name|get
argument_list|(
name|tableName
argument_list|)
operator|.
name|get
argument_list|(
literal|"families"
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|ioe
parameter_list|)
block|{
comment|// in the case of parse error, default to labeling by region
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|table
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
argument_list|()
decl_stmt|;
name|familySet
operator|=
operator|new
name|TreeSet
argument_list|<
name|String
argument_list|>
argument_list|()
expr_stmt|;
name|table
operator|.
name|put
argument_list|(
literal|"families"
argument_list|,
name|familySet
argument_list|)
expr_stmt|;
name|tableInfo
operator|.
name|put
argument_list|(
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
argument_list|,
name|table
argument_list|)
expr_stmt|;
block|}
comment|// we now iterate through each Put and keep track of which families
comment|// are affected in this table.
for|for
control|(
name|Put
name|p
range|:
name|entry
operator|.
name|getValue
argument_list|()
control|)
block|{
for|for
control|(
name|byte
index|[]
name|fam
range|:
name|p
operator|.
name|getFamilyMap
argument_list|()
operator|.
name|keySet
argument_list|()
control|)
block|{
name|familySet
operator|.
name|add
argument_list|(
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|fam
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
name|map
return|;
block|}
comment|/**    * Compile the details beyond the scope of getFingerprint (mostly     * toMap from the Puts) into a Map along with the fingerprinted     * information. Useful for debugging, logging, and administration tools.    * @param maxCols a limit on the number of columns output prior to truncation    * @return Map    */
annotation|@
name|Override
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|toMap
parameter_list|(
name|int
name|maxCols
parameter_list|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|map
init|=
name|getFingerprint
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|tableInfo
init|=
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
operator|)
name|map
operator|.
name|get
argument_list|(
literal|"tables"
argument_list|)
decl_stmt|;
name|int
name|putCount
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|byte
index|[]
argument_list|,
name|List
argument_list|<
name|Put
argument_list|>
argument_list|>
name|entry
range|:
name|puts
operator|.
name|entrySet
argument_list|()
control|)
block|{
comment|// If the limit has been hit for put output, just adjust our counter
if|if
condition|(
name|putCount
operator|>=
name|DEFAULT_MAX_PUT_OUTPUT
condition|)
block|{
name|putCount
operator|+=
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|size
argument_list|()
expr_stmt|;
continue|continue;
block|}
name|List
argument_list|<
name|Put
argument_list|>
name|regionPuts
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
argument_list|>
name|putSummaries
init|=
operator|new
name|ArrayList
argument_list|<
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
comment|// find out how many of this region's puts we can add without busting
comment|// the maximum
name|int
name|regionPutsToAdd
init|=
name|regionPuts
operator|.
name|size
argument_list|()
decl_stmt|;
name|putCount
operator|+=
name|regionPutsToAdd
expr_stmt|;
if|if
condition|(
name|putCount
operator|>
name|DEFAULT_MAX_PUT_OUTPUT
condition|)
block|{
name|regionPutsToAdd
operator|-=
name|putCount
operator|-
name|DEFAULT_MAX_PUT_OUTPUT
expr_stmt|;
block|}
for|for
control|(
name|Iterator
argument_list|<
name|Put
argument_list|>
name|iter
init|=
name|regionPuts
operator|.
name|iterator
argument_list|()
init|;
name|regionPutsToAdd
operator|--
operator|>
literal|0
condition|;
control|)
block|{
name|putSummaries
operator|.
name|add
argument_list|(
name|iter
operator|.
name|next
argument_list|()
operator|.
name|toMap
argument_list|(
name|maxCols
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// attempt to extract the table name from the region name
name|String
name|tableName
init|=
literal|""
decl_stmt|;
try|try
block|{
name|tableName
operator|=
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|HRegionInfo
operator|.
name|parseRegionName
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ioe
parameter_list|)
block|{
comment|// in the case of parse error, default to labeling by region
name|tableName
operator|=
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// since the puts are stored by region, we may have already
comment|// recorded puts for this table. if that is the case,
comment|// we want to add to the existing List. if not, we place a new list
comment|// in the map
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|table
init|=
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
operator|)
name|tableInfo
operator|.
name|get
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
if|if
condition|(
name|table
operator|==
literal|null
condition|)
block|{
comment|// in case the Put has changed since getFingerprint's map was built
name|table
operator|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
argument_list|()
expr_stmt|;
name|tableInfo
operator|.
name|put
argument_list|(
name|tableName
argument_list|,
name|table
argument_list|)
expr_stmt|;
name|table
operator|.
name|put
argument_list|(
literal|"puts"
argument_list|,
name|putSummaries
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|table
operator|.
name|get
argument_list|(
literal|"puts"
argument_list|)
operator|==
literal|null
condition|)
block|{
name|table
operator|.
name|put
argument_list|(
literal|"puts"
argument_list|,
name|putSummaries
argument_list|)
expr_stmt|;
block|}
else|else
block|{
operator|(
operator|(
name|List
argument_list|<
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
argument_list|>
operator|)
name|table
operator|.
name|get
argument_list|(
literal|"puts"
argument_list|)
operator|)
operator|.
name|addAll
argument_list|(
name|putSummaries
argument_list|)
expr_stmt|;
block|}
block|}
name|map
operator|.
name|put
argument_list|(
literal|"totalPuts"
argument_list|,
name|putCount
argument_list|)
expr_stmt|;
return|return
name|map
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|write
parameter_list|(
name|DataOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|writeInt
argument_list|(
name|puts
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|byte
index|[]
argument_list|,
name|List
argument_list|<
name|Put
argument_list|>
argument_list|>
name|e
range|:
name|puts
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|Bytes
operator|.
name|writeByteArray
argument_list|(
name|out
argument_list|,
name|e
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|Put
argument_list|>
name|ps
init|=
name|e
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|out
operator|.
name|writeInt
argument_list|(
name|ps
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|Put
name|p
range|:
name|ps
control|)
block|{
name|p
operator|.
name|write
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|readFields
parameter_list|(
name|DataInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|puts
operator|.
name|clear
argument_list|()
expr_stmt|;
name|int
name|mapSize
init|=
name|in
operator|.
name|readInt
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
name|mapSize
condition|;
name|i
operator|++
control|)
block|{
name|byte
index|[]
name|key
init|=
name|Bytes
operator|.
name|readByteArray
argument_list|(
name|in
argument_list|)
decl_stmt|;
name|int
name|listSize
init|=
name|in
operator|.
name|readInt
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Put
argument_list|>
name|ps
init|=
operator|new
name|ArrayList
argument_list|<
name|Put
argument_list|>
argument_list|(
name|listSize
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|listSize
condition|;
name|j
operator|++
control|)
block|{
name|Put
name|put
init|=
operator|new
name|Put
argument_list|()
decl_stmt|;
name|put
operator|.
name|readFields
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|ps
operator|.
name|add
argument_list|(
name|put
argument_list|)
expr_stmt|;
block|}
name|puts
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|ps
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

