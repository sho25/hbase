begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|util
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
name|hbase
operator|.
name|Cell
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
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|regionserver
operator|.
name|CellSink
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
name|ShipperListener
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

begin_comment
comment|/**  * Specifies methods needed to add elements to a Bloom filter and serialize the  * resulting Bloom filter as a sequence of bytes.  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
interface|interface
name|BloomFilterWriter
extends|extends
name|BloomFilterBase
extends|,
name|CellSink
extends|,
name|ShipperListener
block|{
comment|/** Compact the Bloom filter before writing metadata&amp; data to disk. */
name|void
name|compactBloom
parameter_list|()
function_decl|;
comment|/**    * Get a writable interface into bloom filter meta data.    *    * @return a writable instance that can be later written to a stream    */
name|Writable
name|getMetaWriter
parameter_list|()
function_decl|;
comment|/**    * Get a writable interface into bloom filter data (the actual Bloom bits).    * Not used for compound Bloom filters.    *    * @return a writable instance that can be later written to a stream    */
name|Writable
name|getDataWriter
parameter_list|()
function_decl|;
comment|/**    * Returns the previous cell written by this writer    * @return the previous cell    */
name|Cell
name|getPrevCell
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

