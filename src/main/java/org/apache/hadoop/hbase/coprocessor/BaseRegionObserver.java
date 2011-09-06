begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Copyright 2011 The Apache Software Foundation  *  * Licensed under the Apache License, Version 2.0 (the "License");  * you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
package|;
end_package

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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|CoprocessorEnvironment
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
name|KeyValue
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
name|Delete
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
name|filter
operator|.
name|CompareFilter
operator|.
name|CompareOp
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
name|filter
operator|.
name|WritableByteArrayComparable
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
name|HRegion
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
name|StoreFile
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
name|wal
operator|.
name|HLogKey
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
name|wal
operator|.
name|WALEdit
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

begin_comment
comment|/**  * An abstract class that implements RegionObserver.  * By extending it, you can create your own region observer without  * overriding all abstract methods of RegionObserver.  */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|BaseRegionObserver
implements|implements
name|RegionObserver
block|{
annotation|@
name|Override
specifier|public
name|void
name|start
parameter_list|(
name|CoprocessorEnvironment
name|e
parameter_list|)
block|{ }
annotation|@
name|Override
specifier|public
name|void
name|stop
parameter_list|(
name|CoprocessorEnvironment
name|e
parameter_list|)
block|{ }
annotation|@
name|Override
specifier|public
name|void
name|preOpen
parameter_list|(
name|ObserverContext
argument_list|<
name|RegionCoprocessorEnvironment
argument_list|>
name|e
parameter_list|)
block|{ }
annotation|@
name|Override
specifier|public
name|void
name|postOpen
parameter_list|(
name|ObserverContext
argument_list|<
name|RegionCoprocessorEnvironment
argument_list|>
name|e
parameter_list|)
block|{ }
annotation|@
name|Override
specifier|public
name|void
name|preClose
parameter_list|(
name|ObserverContext
argument_list|<
name|RegionCoprocessorEnvironment
argument_list|>
name|e
parameter_list|,
name|boolean
name|abortRequested
parameter_list|)
block|{ }
annotation|@
name|Override
specifier|public
name|void
name|postClose
parameter_list|(
name|ObserverContext
argument_list|<
name|RegionCoprocessorEnvironment
argument_list|>
name|e
parameter_list|,
name|boolean
name|abortRequested
parameter_list|)
block|{ }
annotation|@
name|Override
specifier|public
name|void
name|preFlush
parameter_list|(
name|ObserverContext
argument_list|<
name|RegionCoprocessorEnvironment
argument_list|>
name|e
parameter_list|)
block|{ }
annotation|@
name|Override
specifier|public
name|void
name|postFlush
parameter_list|(
name|ObserverContext
argument_list|<
name|RegionCoprocessorEnvironment
argument_list|>
name|e
parameter_list|)
block|{ }
annotation|@
name|Override
specifier|public
name|void
name|preSplit
parameter_list|(
name|ObserverContext
argument_list|<
name|RegionCoprocessorEnvironment
argument_list|>
name|e
parameter_list|)
block|{ }
annotation|@
name|Override
specifier|public
name|void
name|postSplit
parameter_list|(
name|ObserverContext
argument_list|<
name|RegionCoprocessorEnvironment
argument_list|>
name|e
parameter_list|,
name|HRegion
name|l
parameter_list|,
name|HRegion
name|r
parameter_list|)
block|{ }
annotation|@
name|Override
specifier|public
name|void
name|preCompactSelection
parameter_list|(
specifier|final
name|ObserverContext
argument_list|<
name|RegionCoprocessorEnvironment
argument_list|>
name|c
parameter_list|,
specifier|final
name|Store
name|store
parameter_list|,
specifier|final
name|List
argument_list|<
name|StoreFile
argument_list|>
name|candidates
parameter_list|)
block|{ }
annotation|@
name|Override
specifier|public
name|void
name|postCompactSelection
parameter_list|(
specifier|final
name|ObserverContext
argument_list|<
name|RegionCoprocessorEnvironment
argument_list|>
name|c
parameter_list|,
specifier|final
name|Store
name|store
parameter_list|,
specifier|final
name|ImmutableList
argument_list|<
name|StoreFile
argument_list|>
name|selected
parameter_list|)
block|{ }
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
name|e
parameter_list|,
specifier|final
name|Store
name|store
parameter_list|,
specifier|final
name|InternalScanner
name|scanner
parameter_list|)
block|{
return|return
name|scanner
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|postCompact
parameter_list|(
name|ObserverContext
argument_list|<
name|RegionCoprocessorEnvironment
argument_list|>
name|e
parameter_list|,
specifier|final
name|Store
name|store
parameter_list|,
specifier|final
name|StoreFile
name|resultFile
parameter_list|)
block|{ }
annotation|@
name|Override
specifier|public
name|void
name|preGetClosestRowBefore
parameter_list|(
specifier|final
name|ObserverContext
argument_list|<
name|RegionCoprocessorEnvironment
argument_list|>
name|e
parameter_list|,
specifier|final
name|byte
index|[]
name|row
parameter_list|,
specifier|final
name|byte
index|[]
name|family
parameter_list|,
specifier|final
name|Result
name|result
parameter_list|)
throws|throws
name|IOException
block|{   }
annotation|@
name|Override
specifier|public
name|void
name|postGetClosestRowBefore
parameter_list|(
specifier|final
name|ObserverContext
argument_list|<
name|RegionCoprocessorEnvironment
argument_list|>
name|e
parameter_list|,
specifier|final
name|byte
index|[]
name|row
parameter_list|,
specifier|final
name|byte
index|[]
name|family
parameter_list|,
specifier|final
name|Result
name|result
parameter_list|)
throws|throws
name|IOException
block|{   }
annotation|@
name|Override
specifier|public
name|void
name|preGet
parameter_list|(
specifier|final
name|ObserverContext
argument_list|<
name|RegionCoprocessorEnvironment
argument_list|>
name|e
parameter_list|,
specifier|final
name|Get
name|get
parameter_list|,
specifier|final
name|List
argument_list|<
name|KeyValue
argument_list|>
name|results
parameter_list|)
throws|throws
name|IOException
block|{   }
annotation|@
name|Override
specifier|public
name|void
name|postGet
parameter_list|(
specifier|final
name|ObserverContext
argument_list|<
name|RegionCoprocessorEnvironment
argument_list|>
name|e
parameter_list|,
specifier|final
name|Get
name|get
parameter_list|,
specifier|final
name|List
argument_list|<
name|KeyValue
argument_list|>
name|results
parameter_list|)
throws|throws
name|IOException
block|{   }
annotation|@
name|Override
specifier|public
name|boolean
name|preExists
parameter_list|(
specifier|final
name|ObserverContext
argument_list|<
name|RegionCoprocessorEnvironment
argument_list|>
name|e
parameter_list|,
specifier|final
name|Get
name|get
parameter_list|,
specifier|final
name|boolean
name|exists
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|exists
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|postExists
parameter_list|(
specifier|final
name|ObserverContext
argument_list|<
name|RegionCoprocessorEnvironment
argument_list|>
name|e
parameter_list|,
specifier|final
name|Get
name|get
parameter_list|,
name|boolean
name|exists
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|exists
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|prePut
parameter_list|(
specifier|final
name|ObserverContext
argument_list|<
name|RegionCoprocessorEnvironment
argument_list|>
name|e
parameter_list|,
specifier|final
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|List
argument_list|<
name|KeyValue
argument_list|>
argument_list|>
name|familyMap
parameter_list|,
specifier|final
name|boolean
name|writeToWAL
parameter_list|)
throws|throws
name|IOException
block|{   }
annotation|@
name|Override
specifier|public
name|void
name|postPut
parameter_list|(
specifier|final
name|ObserverContext
argument_list|<
name|RegionCoprocessorEnvironment
argument_list|>
name|e
parameter_list|,
specifier|final
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|List
argument_list|<
name|KeyValue
argument_list|>
argument_list|>
name|familyMap
parameter_list|,
specifier|final
name|boolean
name|writeToWAL
parameter_list|)
throws|throws
name|IOException
block|{   }
annotation|@
name|Override
specifier|public
name|void
name|preDelete
parameter_list|(
specifier|final
name|ObserverContext
argument_list|<
name|RegionCoprocessorEnvironment
argument_list|>
name|e
parameter_list|,
specifier|final
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|List
argument_list|<
name|KeyValue
argument_list|>
argument_list|>
name|familyMap
parameter_list|,
specifier|final
name|boolean
name|writeToWAL
parameter_list|)
throws|throws
name|IOException
block|{   }
annotation|@
name|Override
specifier|public
name|void
name|postDelete
parameter_list|(
specifier|final
name|ObserverContext
argument_list|<
name|RegionCoprocessorEnvironment
argument_list|>
name|e
parameter_list|,
specifier|final
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|List
argument_list|<
name|KeyValue
argument_list|>
argument_list|>
name|familyMap
parameter_list|,
specifier|final
name|boolean
name|writeToWAL
parameter_list|)
throws|throws
name|IOException
block|{   }
annotation|@
name|Override
specifier|public
name|boolean
name|preCheckAndPut
parameter_list|(
specifier|final
name|ObserverContext
argument_list|<
name|RegionCoprocessorEnvironment
argument_list|>
name|e
parameter_list|,
specifier|final
name|byte
index|[]
name|row
parameter_list|,
specifier|final
name|byte
index|[]
name|family
parameter_list|,
specifier|final
name|byte
index|[]
name|qualifier
parameter_list|,
specifier|final
name|CompareOp
name|compareOp
parameter_list|,
specifier|final
name|WritableByteArrayComparable
name|comparator
parameter_list|,
specifier|final
name|Put
name|put
parameter_list|,
specifier|final
name|boolean
name|result
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|result
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|postCheckAndPut
parameter_list|(
specifier|final
name|ObserverContext
argument_list|<
name|RegionCoprocessorEnvironment
argument_list|>
name|e
parameter_list|,
specifier|final
name|byte
index|[]
name|row
parameter_list|,
specifier|final
name|byte
index|[]
name|family
parameter_list|,
specifier|final
name|byte
index|[]
name|qualifier
parameter_list|,
specifier|final
name|CompareOp
name|compareOp
parameter_list|,
specifier|final
name|WritableByteArrayComparable
name|comparator
parameter_list|,
specifier|final
name|Put
name|put
parameter_list|,
specifier|final
name|boolean
name|result
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|result
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|preCheckAndDelete
parameter_list|(
specifier|final
name|ObserverContext
argument_list|<
name|RegionCoprocessorEnvironment
argument_list|>
name|e
parameter_list|,
specifier|final
name|byte
index|[]
name|row
parameter_list|,
specifier|final
name|byte
index|[]
name|family
parameter_list|,
specifier|final
name|byte
index|[]
name|qualifier
parameter_list|,
specifier|final
name|CompareOp
name|compareOp
parameter_list|,
specifier|final
name|WritableByteArrayComparable
name|comparator
parameter_list|,
specifier|final
name|Delete
name|delete
parameter_list|,
specifier|final
name|boolean
name|result
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|result
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|postCheckAndDelete
parameter_list|(
specifier|final
name|ObserverContext
argument_list|<
name|RegionCoprocessorEnvironment
argument_list|>
name|e
parameter_list|,
specifier|final
name|byte
index|[]
name|row
parameter_list|,
specifier|final
name|byte
index|[]
name|family
parameter_list|,
specifier|final
name|byte
index|[]
name|qualifier
parameter_list|,
specifier|final
name|CompareOp
name|compareOp
parameter_list|,
specifier|final
name|WritableByteArrayComparable
name|comparator
parameter_list|,
specifier|final
name|Delete
name|delete
parameter_list|,
specifier|final
name|boolean
name|result
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|result
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|preIncrementColumnValue
parameter_list|(
specifier|final
name|ObserverContext
argument_list|<
name|RegionCoprocessorEnvironment
argument_list|>
name|e
parameter_list|,
specifier|final
name|byte
index|[]
name|row
parameter_list|,
specifier|final
name|byte
index|[]
name|family
parameter_list|,
specifier|final
name|byte
index|[]
name|qualifier
parameter_list|,
specifier|final
name|long
name|amount
parameter_list|,
specifier|final
name|boolean
name|writeToWAL
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|amount
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|postIncrementColumnValue
parameter_list|(
specifier|final
name|ObserverContext
argument_list|<
name|RegionCoprocessorEnvironment
argument_list|>
name|e
parameter_list|,
specifier|final
name|byte
index|[]
name|row
parameter_list|,
specifier|final
name|byte
index|[]
name|family
parameter_list|,
specifier|final
name|byte
index|[]
name|qualifier
parameter_list|,
specifier|final
name|long
name|amount
parameter_list|,
specifier|final
name|boolean
name|writeToWAL
parameter_list|,
name|long
name|result
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|result
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|preIncrement
parameter_list|(
specifier|final
name|ObserverContext
argument_list|<
name|RegionCoprocessorEnvironment
argument_list|>
name|e
parameter_list|,
specifier|final
name|Increment
name|increment
parameter_list|,
specifier|final
name|Result
name|result
parameter_list|)
throws|throws
name|IOException
block|{   }
annotation|@
name|Override
specifier|public
name|void
name|postIncrement
parameter_list|(
specifier|final
name|ObserverContext
argument_list|<
name|RegionCoprocessorEnvironment
argument_list|>
name|e
parameter_list|,
specifier|final
name|Increment
name|increment
parameter_list|,
specifier|final
name|Result
name|result
parameter_list|)
throws|throws
name|IOException
block|{   }
annotation|@
name|Override
specifier|public
name|RegionScanner
name|preScannerOpen
parameter_list|(
specifier|final
name|ObserverContext
argument_list|<
name|RegionCoprocessorEnvironment
argument_list|>
name|e
parameter_list|,
specifier|final
name|Scan
name|scan
parameter_list|,
specifier|final
name|RegionScanner
name|s
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|s
return|;
block|}
annotation|@
name|Override
specifier|public
name|RegionScanner
name|postScannerOpen
parameter_list|(
specifier|final
name|ObserverContext
argument_list|<
name|RegionCoprocessorEnvironment
argument_list|>
name|e
parameter_list|,
specifier|final
name|Scan
name|scan
parameter_list|,
specifier|final
name|RegionScanner
name|s
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|s
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|preScannerNext
parameter_list|(
specifier|final
name|ObserverContext
argument_list|<
name|RegionCoprocessorEnvironment
argument_list|>
name|e
parameter_list|,
specifier|final
name|InternalScanner
name|s
parameter_list|,
specifier|final
name|List
argument_list|<
name|Result
argument_list|>
name|results
parameter_list|,
specifier|final
name|int
name|limit
parameter_list|,
specifier|final
name|boolean
name|hasMore
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|hasMore
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|postScannerNext
parameter_list|(
specifier|final
name|ObserverContext
argument_list|<
name|RegionCoprocessorEnvironment
argument_list|>
name|e
parameter_list|,
specifier|final
name|InternalScanner
name|s
parameter_list|,
specifier|final
name|List
argument_list|<
name|Result
argument_list|>
name|results
parameter_list|,
specifier|final
name|int
name|limit
parameter_list|,
specifier|final
name|boolean
name|hasMore
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|hasMore
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|preScannerClose
parameter_list|(
specifier|final
name|ObserverContext
argument_list|<
name|RegionCoprocessorEnvironment
argument_list|>
name|e
parameter_list|,
specifier|final
name|InternalScanner
name|s
parameter_list|)
throws|throws
name|IOException
block|{   }
annotation|@
name|Override
specifier|public
name|void
name|postScannerClose
parameter_list|(
specifier|final
name|ObserverContext
argument_list|<
name|RegionCoprocessorEnvironment
argument_list|>
name|e
parameter_list|,
specifier|final
name|InternalScanner
name|s
parameter_list|)
throws|throws
name|IOException
block|{   }
annotation|@
name|Override
specifier|public
name|void
name|preWALRestore
parameter_list|(
name|ObserverContext
argument_list|<
name|RegionCoprocessorEnvironment
argument_list|>
name|env
parameter_list|,
name|HRegionInfo
name|info
parameter_list|,
name|HLogKey
name|logKey
parameter_list|,
name|WALEdit
name|logEdit
parameter_list|)
throws|throws
name|IOException
block|{   }
annotation|@
name|Override
specifier|public
name|void
name|postWALRestore
parameter_list|(
name|ObserverContext
argument_list|<
name|RegionCoprocessorEnvironment
argument_list|>
name|env
parameter_list|,
name|HRegionInfo
name|info
parameter_list|,
name|HLogKey
name|logKey
parameter_list|,
name|WALEdit
name|logEdit
parameter_list|)
throws|throws
name|IOException
block|{   }
block|}
end_class

end_unit

