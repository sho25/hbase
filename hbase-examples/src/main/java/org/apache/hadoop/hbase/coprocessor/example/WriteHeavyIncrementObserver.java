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
name|coprocessor
operator|.
name|example
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
name|java
operator|.
name|math
operator|.
name|RoundingMode
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
name|NavigableMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Optional
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
name|stream
operator|.
name|IntStream
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|lang3
operator|.
name|mutable
operator|.
name|MutableLong
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
name|Cell
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
name|CellBuilderFactory
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
name|CellBuilderType
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
name|CellUtil
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
name|client
operator|.
name|Get
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
name|client
operator|.
name|Increment
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
name|client
operator|.
name|Put
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
name|client
operator|.
name|Result
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
name|client
operator|.
name|Scan
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
name|coprocessor
operator|.
name|ObserverContext
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
name|coprocessor
operator|.
name|RegionCoprocessor
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
name|coprocessor
operator|.
name|RegionCoprocessorEnvironment
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
name|coprocessor
operator|.
name|RegionObserver
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
name|FlushLifeCycleTracker
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
name|InternalScanner
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
name|RegionScanner
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
name|ScanOptions
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
name|ScanType
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
name|ScannerContext
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
name|Store
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
name|CompactionLifeCycleTracker
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
name|CompactionRequest
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
name|shaded
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|math
operator|.
name|IntMath
import|;
end_import

begin_comment
comment|/**  * An example for implementing a counter that reads is much less than writes, i.e, write heavy.  *<p>  * We will convert increment to put, and do aggregating when get. And of course the return value of  * increment is useless then.  *<p>  * Notice that this is only an example so we do not handle most corner cases, for example, you must  * provide a qualifier when doing a get.  */
end_comment

begin_class
specifier|public
class|class
name|WriteHeavyIncrementObserver
implements|implements
name|RegionCoprocessor
implements|,
name|RegionObserver
block|{
annotation|@
name|Override
specifier|public
name|Optional
argument_list|<
name|RegionObserver
argument_list|>
name|getRegionObserver
parameter_list|()
block|{
return|return
name|Optional
operator|.
name|of
argument_list|(
name|this
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|preFlushScannerOpen
parameter_list|(
name|ObserverContext
argument_list|<
name|RegionCoprocessorEnvironment
argument_list|>
name|c
parameter_list|,
name|Store
name|store
parameter_list|,
name|ScanOptions
name|options
parameter_list|,
name|FlushLifeCycleTracker
name|tracker
parameter_list|)
throws|throws
name|IOException
block|{
name|options
operator|.
name|readAllVersions
argument_list|()
expr_stmt|;
block|}
specifier|private
name|Cell
name|createCell
parameter_list|(
name|byte
index|[]
name|row
parameter_list|,
name|byte
index|[]
name|family
parameter_list|,
name|byte
index|[]
name|qualifier
parameter_list|,
name|long
name|ts
parameter_list|,
name|long
name|value
parameter_list|)
block|{
return|return
name|CellBuilderFactory
operator|.
name|create
argument_list|(
name|CellBuilderType
operator|.
name|SHALLOW_COPY
argument_list|)
operator|.
name|setRow
argument_list|(
name|row
argument_list|)
operator|.
name|setType
argument_list|(
name|Cell
operator|.
name|Type
operator|.
name|Put
argument_list|)
operator|.
name|setFamily
argument_list|(
name|family
argument_list|)
operator|.
name|setQualifier
argument_list|(
name|qualifier
argument_list|)
operator|.
name|setTimestamp
argument_list|(
name|ts
argument_list|)
operator|.
name|setValue
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|value
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
specifier|private
name|InternalScanner
name|wrap
parameter_list|(
name|byte
index|[]
name|family
parameter_list|,
name|InternalScanner
name|scanner
parameter_list|)
block|{
return|return
operator|new
name|InternalScanner
argument_list|()
block|{
specifier|private
name|List
argument_list|<
name|Cell
argument_list|>
name|srcResult
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
specifier|private
name|byte
index|[]
name|row
decl_stmt|;
specifier|private
name|byte
index|[]
name|qualifier
decl_stmt|;
specifier|private
name|long
name|timestamp
decl_stmt|;
specifier|private
name|long
name|sum
decl_stmt|;
annotation|@
name|Override
specifier|public
name|boolean
name|next
parameter_list|(
name|List
argument_list|<
name|Cell
argument_list|>
name|result
parameter_list|,
name|ScannerContext
name|scannerContext
parameter_list|)
throws|throws
name|IOException
block|{
name|boolean
name|moreRows
init|=
name|scanner
operator|.
name|next
argument_list|(
name|srcResult
argument_list|,
name|scannerContext
argument_list|)
decl_stmt|;
if|if
condition|(
name|srcResult
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
if|if
condition|(
operator|!
name|moreRows
operator|&&
name|row
operator|!=
literal|null
condition|)
block|{
name|result
operator|.
name|add
argument_list|(
name|createCell
argument_list|(
name|row
argument_list|,
name|family
argument_list|,
name|qualifier
argument_list|,
name|timestamp
argument_list|,
name|sum
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|moreRows
return|;
block|}
name|Cell
name|firstCell
init|=
name|srcResult
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
comment|// Check if there is a row change first. All the cells will come from the same row so just
comment|// check the first one once is enough.
if|if
condition|(
name|row
operator|==
literal|null
condition|)
block|{
name|row
operator|=
name|CellUtil
operator|.
name|cloneRow
argument_list|(
name|firstCell
argument_list|)
expr_stmt|;
name|qualifier
operator|=
name|CellUtil
operator|.
name|cloneQualifier
argument_list|(
name|firstCell
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
operator|!
name|CellUtil
operator|.
name|matchingRows
argument_list|(
name|firstCell
argument_list|,
name|row
argument_list|)
condition|)
block|{
name|result
operator|.
name|add
argument_list|(
name|createCell
argument_list|(
name|row
argument_list|,
name|family
argument_list|,
name|qualifier
argument_list|,
name|timestamp
argument_list|,
name|sum
argument_list|)
argument_list|)
expr_stmt|;
name|row
operator|=
name|CellUtil
operator|.
name|cloneRow
argument_list|(
name|firstCell
argument_list|)
expr_stmt|;
name|qualifier
operator|=
name|CellUtil
operator|.
name|cloneQualifier
argument_list|(
name|firstCell
argument_list|)
expr_stmt|;
name|sum
operator|=
literal|0
expr_stmt|;
block|}
name|srcResult
operator|.
name|forEach
argument_list|(
name|c
lambda|->
block|{
if|if
condition|(
name|CellUtil
operator|.
name|matchingQualifier
argument_list|(
name|c
argument_list|,
name|qualifier
argument_list|)
condition|)
block|{
name|sum
operator|+=
name|Bytes
operator|.
name|toLong
argument_list|(
name|c
operator|.
name|getValueArray
argument_list|()
argument_list|,
name|c
operator|.
name|getValueOffset
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|result
operator|.
name|add
argument_list|(
name|createCell
argument_list|(
name|row
argument_list|,
name|family
argument_list|,
name|qualifier
argument_list|,
name|timestamp
argument_list|,
name|sum
argument_list|)
argument_list|)
expr_stmt|;
name|qualifier
operator|=
name|CellUtil
operator|.
name|cloneQualifier
argument_list|(
name|c
argument_list|)
expr_stmt|;
name|sum
operator|=
name|Bytes
operator|.
name|toLong
argument_list|(
name|c
operator|.
name|getValueArray
argument_list|()
argument_list|,
name|c
operator|.
name|getValueOffset
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|timestamp
operator|=
name|c
operator|.
name|getTimestamp
argument_list|()
expr_stmt|;
block|}
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|moreRows
condition|)
block|{
name|result
operator|.
name|add
argument_list|(
name|createCell
argument_list|(
name|row
argument_list|,
name|family
argument_list|,
name|qualifier
argument_list|,
name|timestamp
argument_list|,
name|sum
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|srcResult
operator|.
name|clear
argument_list|()
expr_stmt|;
return|return
name|moreRows
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{
name|scanner
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
return|;
block|}
annotation|@
name|Override
specifier|public
name|InternalScanner
name|preFlush
parameter_list|(
name|ObserverContext
argument_list|<
name|RegionCoprocessorEnvironment
argument_list|>
name|c
parameter_list|,
name|Store
name|store
parameter_list|,
name|InternalScanner
name|scanner
parameter_list|,
name|FlushLifeCycleTracker
name|tracker
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|wrap
argument_list|(
name|store
operator|.
name|getColumnFamilyDescriptor
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|,
name|scanner
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|preCompactScannerOpen
parameter_list|(
name|ObserverContext
argument_list|<
name|RegionCoprocessorEnvironment
argument_list|>
name|c
parameter_list|,
name|Store
name|store
parameter_list|,
name|ScanType
name|scanType
parameter_list|,
name|ScanOptions
name|options
parameter_list|,
name|CompactionLifeCycleTracker
name|tracker
parameter_list|,
name|CompactionRequest
name|request
parameter_list|)
throws|throws
name|IOException
block|{
name|options
operator|.
name|readAllVersions
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|InternalScanner
name|preCompact
parameter_list|(
name|ObserverContext
argument_list|<
name|RegionCoprocessorEnvironment
argument_list|>
name|c
parameter_list|,
name|Store
name|store
parameter_list|,
name|InternalScanner
name|scanner
parameter_list|,
name|ScanType
name|scanType
parameter_list|,
name|CompactionLifeCycleTracker
name|tracker
parameter_list|,
name|CompactionRequest
name|request
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|wrap
argument_list|(
name|store
operator|.
name|getColumnFamilyDescriptor
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|,
name|scanner
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|preMemStoreCompactionCompactScannerOpen
parameter_list|(
name|ObserverContext
argument_list|<
name|RegionCoprocessorEnvironment
argument_list|>
name|c
parameter_list|,
name|Store
name|store
parameter_list|,
name|ScanOptions
name|options
parameter_list|)
throws|throws
name|IOException
block|{
name|options
operator|.
name|readAllVersions
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|InternalScanner
name|preMemStoreCompactionCompact
parameter_list|(
name|ObserverContext
argument_list|<
name|RegionCoprocessorEnvironment
argument_list|>
name|c
parameter_list|,
name|Store
name|store
parameter_list|,
name|InternalScanner
name|scanner
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|wrap
argument_list|(
name|store
operator|.
name|getColumnFamilyDescriptor
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|,
name|scanner
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|preGetOp
parameter_list|(
name|ObserverContext
argument_list|<
name|RegionCoprocessorEnvironment
argument_list|>
name|c
parameter_list|,
name|Get
name|get
parameter_list|,
name|List
argument_list|<
name|Cell
argument_list|>
name|result
parameter_list|)
throws|throws
name|IOException
block|{
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|()
operator|.
name|withStartRow
argument_list|(
name|get
operator|.
name|getRow
argument_list|()
argument_list|)
operator|.
name|withStopRow
argument_list|(
name|get
operator|.
name|getRow
argument_list|()
argument_list|,
literal|true
argument_list|)
operator|.
name|readAllVersions
argument_list|()
decl_stmt|;
name|NavigableMap
argument_list|<
name|byte
index|[]
argument_list|,
name|NavigableMap
argument_list|<
name|byte
index|[]
argument_list|,
name|MutableLong
argument_list|>
argument_list|>
name|sums
init|=
operator|new
name|TreeMap
argument_list|<>
argument_list|(
name|Bytes
operator|.
name|BYTES_COMPARATOR
argument_list|)
decl_stmt|;
name|get
operator|.
name|getFamilyMap
argument_list|()
operator|.
name|forEach
argument_list|(
parameter_list|(
name|cf
parameter_list|,
name|cqs
parameter_list|)
lambda|->
block|{
name|NavigableMap
argument_list|<
name|byte
index|[]
argument_list|,
name|MutableLong
argument_list|>
name|ss
init|=
operator|new
name|TreeMap
argument_list|<>
argument_list|(
name|Bytes
operator|.
name|BYTES_COMPARATOR
argument_list|)
decl_stmt|;
name|sums
operator|.
name|put
argument_list|(
name|cf
argument_list|,
name|ss
argument_list|)
expr_stmt|;
name|cqs
operator|.
name|forEach
argument_list|(
name|cq
lambda|->
block|{
name|ss
operator|.
name|put
argument_list|(
name|cq
argument_list|,
operator|new
name|MutableLong
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|scan
operator|.
name|addColumn
argument_list|(
name|cf
argument_list|,
name|cq
argument_list|)
expr_stmt|;
block|}
argument_list|)
expr_stmt|;
block|}
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|Cell
argument_list|>
name|cells
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
try|try
init|(
name|RegionScanner
name|scanner
init|=
name|c
operator|.
name|getEnvironment
argument_list|()
operator|.
name|getRegion
argument_list|()
operator|.
name|getScanner
argument_list|(
name|scan
argument_list|)
init|)
block|{
name|boolean
name|moreRows
decl_stmt|;
do|do
block|{
name|moreRows
operator|=
name|scanner
operator|.
name|next
argument_list|(
name|cells
argument_list|)
expr_stmt|;
for|for
control|(
name|Cell
name|cell
range|:
name|cells
control|)
block|{
name|byte
index|[]
name|family
init|=
name|CellUtil
operator|.
name|cloneFamily
argument_list|(
name|cell
argument_list|)
decl_stmt|;
name|byte
index|[]
name|qualifier
init|=
name|CellUtil
operator|.
name|cloneQualifier
argument_list|(
name|cell
argument_list|)
decl_stmt|;
name|long
name|value
init|=
name|Bytes
operator|.
name|toLong
argument_list|(
name|cell
operator|.
name|getValueArray
argument_list|()
argument_list|,
name|cell
operator|.
name|getValueOffset
argument_list|()
argument_list|)
decl_stmt|;
name|sums
operator|.
name|get
argument_list|(
name|family
argument_list|)
operator|.
name|get
argument_list|(
name|qualifier
argument_list|)
operator|.
name|add
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
name|cells
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
do|while
condition|(
name|moreRows
condition|)
do|;
block|}
name|sums
operator|.
name|forEach
argument_list|(
parameter_list|(
name|cf
parameter_list|,
name|m
parameter_list|)
lambda|->
name|m
operator|.
name|forEach
argument_list|(
parameter_list|(
name|cq
parameter_list|,
name|s
parameter_list|)
lambda|->
name|result
operator|.
name|add
argument_list|(
name|createCell
argument_list|(
name|get
operator|.
name|getRow
argument_list|()
argument_list|,
name|cf
argument_list|,
name|cq
argument_list|,
name|HConstants
operator|.
name|LATEST_TIMESTAMP
argument_list|,
name|s
operator|.
name|longValue
argument_list|()
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|c
operator|.
name|bypass
argument_list|()
expr_stmt|;
block|}
specifier|private
specifier|final
name|int
name|mask
decl_stmt|;
specifier|private
specifier|final
name|MutableLong
index|[]
name|lastTimestamps
decl_stmt|;
block|{
name|int
name|stripes
init|=
literal|1
operator|<<
name|IntMath
operator|.
name|log2
argument_list|(
name|Runtime
operator|.
name|getRuntime
argument_list|()
operator|.
name|availableProcessors
argument_list|()
argument_list|,
name|RoundingMode
operator|.
name|CEILING
argument_list|)
decl_stmt|;
name|lastTimestamps
operator|=
name|IntStream
operator|.
name|range
argument_list|(
literal|0
argument_list|,
name|stripes
argument_list|)
operator|.
name|mapToObj
argument_list|(
name|i
lambda|->
operator|new
name|MutableLong
argument_list|()
argument_list|)
operator|.
name|toArray
argument_list|(
name|MutableLong
index|[]
operator|::
operator|new
argument_list|)
expr_stmt|;
name|mask
operator|=
name|stripes
operator|-
literal|1
expr_stmt|;
block|}
comment|// We need make sure the different put uses different timestamp otherwise we may lost some
comment|// increments. This is a known issue for HBase.
specifier|private
name|long
name|getUniqueTimestamp
parameter_list|(
name|byte
index|[]
name|row
parameter_list|)
block|{
name|int
name|slot
init|=
name|Bytes
operator|.
name|hashCode
argument_list|(
name|row
argument_list|)
operator|&
name|mask
decl_stmt|;
name|MutableLong
name|lastTimestamp
init|=
name|lastTimestamps
index|[
name|slot
index|]
decl_stmt|;
name|long
name|now
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
synchronized|synchronized
init|(
name|lastTimestamp
init|)
block|{
name|long
name|pt
init|=
name|lastTimestamp
operator|.
name|longValue
argument_list|()
operator|>>
literal|10
decl_stmt|;
if|if
condition|(
name|now
operator|>
name|pt
condition|)
block|{
name|lastTimestamp
operator|.
name|setValue
argument_list|(
name|now
operator|<<
literal|10
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|lastTimestamp
operator|.
name|increment
argument_list|()
expr_stmt|;
block|}
return|return
name|lastTimestamp
operator|.
name|longValue
argument_list|()
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|Result
name|preIncrement
parameter_list|(
name|ObserverContext
argument_list|<
name|RegionCoprocessorEnvironment
argument_list|>
name|c
parameter_list|,
name|Increment
name|increment
parameter_list|)
throws|throws
name|IOException
block|{
name|byte
index|[]
name|row
init|=
name|increment
operator|.
name|getRow
argument_list|()
decl_stmt|;
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|row
argument_list|)
decl_stmt|;
name|long
name|ts
init|=
name|getUniqueTimestamp
argument_list|(
name|row
argument_list|)
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
name|Cell
argument_list|>
argument_list|>
name|entry
range|:
name|increment
operator|.
name|getFamilyCellMap
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
for|for
control|(
name|Cell
name|cell
range|:
name|entry
operator|.
name|getValue
argument_list|()
control|)
block|{
name|put
operator|.
name|add
argument_list|(
name|CellBuilderFactory
operator|.
name|create
argument_list|(
name|CellBuilderType
operator|.
name|SHALLOW_COPY
argument_list|)
operator|.
name|setRow
argument_list|(
name|row
argument_list|)
operator|.
name|setFamily
argument_list|(
name|cell
operator|.
name|getFamilyArray
argument_list|()
argument_list|,
name|cell
operator|.
name|getFamilyOffset
argument_list|()
argument_list|,
name|cell
operator|.
name|getFamilyLength
argument_list|()
argument_list|)
operator|.
name|setQualifier
argument_list|(
name|cell
operator|.
name|getQualifierArray
argument_list|()
argument_list|,
name|cell
operator|.
name|getQualifierOffset
argument_list|()
argument_list|,
name|cell
operator|.
name|getQualifierLength
argument_list|()
argument_list|)
operator|.
name|setValue
argument_list|(
name|cell
operator|.
name|getValueArray
argument_list|()
argument_list|,
name|cell
operator|.
name|getValueOffset
argument_list|()
argument_list|,
name|cell
operator|.
name|getValueLength
argument_list|()
argument_list|)
operator|.
name|setType
argument_list|(
name|Cell
operator|.
name|Type
operator|.
name|Put
argument_list|)
operator|.
name|setTimestamp
argument_list|(
name|ts
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|c
operator|.
name|getEnvironment
argument_list|()
operator|.
name|getRegion
argument_list|()
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
name|c
operator|.
name|bypass
argument_list|()
expr_stmt|;
return|return
name|Result
operator|.
name|EMPTY_RESULT
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|preStoreScannerOpen
parameter_list|(
name|ObserverContext
argument_list|<
name|RegionCoprocessorEnvironment
argument_list|>
name|ctx
parameter_list|,
name|Store
name|store
parameter_list|,
name|ScanOptions
name|options
parameter_list|)
throws|throws
name|IOException
block|{
name|options
operator|.
name|readAllVersions
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

