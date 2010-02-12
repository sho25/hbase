begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Copyright 2009 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|io
operator|.
name|Writable
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
name|hbase
operator|.
name|HServerAddress
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
name|io
operator|.
name|DataInput
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
name|TreeMap
import|;
end_import

begin_class
specifier|public
class|class
name|MultiPut
implements|implements
name|Writable
block|{
specifier|public
name|HServerAddress
name|address
decl_stmt|;
comment|// client code ONLY
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
specifier|public
name|MultiPut
parameter_list|()
block|{}
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

