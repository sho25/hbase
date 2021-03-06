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
name|util
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
name|assertNotNull
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
name|java
operator|.
name|util
operator|.
name|Random
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
name|testclassification
operator|.
name|MediumTests
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
name|util
operator|.
name|AvlUtil
operator|.
name|AvlIterableList
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
name|AvlUtil
operator|.
name|AvlKeyComparator
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
name|AvlUtil
operator|.
name|AvlLinkedNode
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
name|AvlUtil
operator|.
name|AvlNode
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
name|AvlUtil
operator|.
name|AvlNodeVisitor
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
name|AvlUtil
operator|.
name|AvlTree
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
name|AvlUtil
operator|.
name|AvlTreeIterator
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
name|MediumTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestAvlUtil
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
name|TestAvlUtil
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|TestAvlKeyComparator
name|KEY_COMPARATOR
init|=
operator|new
name|TestAvlKeyComparator
argument_list|()
decl_stmt|;
annotation|@
name|Test
specifier|public
name|void
name|testAvlTreeCrud
parameter_list|()
block|{
specifier|final
name|int
name|MAX_KEY
init|=
literal|99999999
decl_stmt|;
specifier|final
name|int
name|NELEM
init|=
literal|10000
decl_stmt|;
specifier|final
name|TreeMap
argument_list|<
name|Integer
argument_list|,
name|Object
argument_list|>
name|treeMap
init|=
operator|new
name|TreeMap
argument_list|<>
argument_list|()
decl_stmt|;
name|TestAvlNode
name|root
init|=
literal|null
decl_stmt|;
specifier|final
name|Random
name|rand
init|=
operator|new
name|Random
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
name|NELEM
condition|;
operator|++
name|i
control|)
block|{
name|int
name|key
init|=
name|rand
operator|.
name|nextInt
argument_list|(
name|MAX_KEY
argument_list|)
decl_stmt|;
if|if
condition|(
name|AvlTree
operator|.
name|get
argument_list|(
name|root
argument_list|,
name|key
argument_list|,
name|KEY_COMPARATOR
argument_list|)
operator|!=
literal|null
condition|)
block|{
name|i
operator|--
expr_stmt|;
continue|continue;
block|}
name|root
operator|=
name|AvlTree
operator|.
name|insert
argument_list|(
name|root
argument_list|,
operator|new
name|TestAvlNode
argument_list|(
name|key
argument_list|)
argument_list|)
expr_stmt|;
name|treeMap
operator|.
name|put
argument_list|(
name|key
argument_list|,
literal|null
argument_list|)
expr_stmt|;
for|for
control|(
name|Integer
name|keyX
range|:
name|treeMap
operator|.
name|keySet
argument_list|()
control|)
block|{
name|TestAvlNode
name|node
init|=
name|AvlTree
operator|.
name|get
argument_list|(
name|root
argument_list|,
name|keyX
argument_list|,
name|KEY_COMPARATOR
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
name|node
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|keyX
operator|.
name|intValue
argument_list|()
argument_list|,
name|node
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|NELEM
condition|;
operator|++
name|i
control|)
block|{
name|int
name|key
init|=
name|rand
operator|.
name|nextInt
argument_list|(
name|MAX_KEY
argument_list|)
decl_stmt|;
name|TestAvlNode
name|node
init|=
name|AvlTree
operator|.
name|get
argument_list|(
name|root
argument_list|,
name|key
argument_list|,
name|KEY_COMPARATOR
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|treeMap
operator|.
name|containsKey
argument_list|(
name|key
argument_list|)
condition|)
block|{
assert|assert
name|node
operator|==
literal|null
assert|;
continue|continue;
block|}
name|treeMap
operator|.
name|remove
argument_list|(
name|key
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|key
argument_list|,
name|node
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
name|root
operator|=
name|AvlTree
operator|.
name|remove
argument_list|(
name|root
argument_list|,
name|key
argument_list|,
name|KEY_COMPARATOR
argument_list|)
expr_stmt|;
for|for
control|(
name|Integer
name|keyX
range|:
name|treeMap
operator|.
name|keySet
argument_list|()
control|)
block|{
name|node
operator|=
name|AvlTree
operator|.
name|get
argument_list|(
name|root
argument_list|,
name|keyX
argument_list|,
name|KEY_COMPARATOR
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|node
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|keyX
operator|.
name|intValue
argument_list|()
argument_list|,
name|node
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testAvlTreeVisitor
parameter_list|()
block|{
specifier|final
name|int
name|MIN_KEY
init|=
literal|0
decl_stmt|;
specifier|final
name|int
name|MAX_KEY
init|=
literal|50
decl_stmt|;
name|TestAvlNode
name|root
init|=
literal|null
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
name|MAX_KEY
init|;
name|i
operator|>=
name|MIN_KEY
condition|;
operator|--
name|i
control|)
block|{
name|root
operator|=
name|AvlTree
operator|.
name|insert
argument_list|(
name|root
argument_list|,
operator|new
name|TestAvlNode
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|AvlTree
operator|.
name|visit
argument_list|(
name|root
argument_list|,
operator|new
name|AvlNodeVisitor
argument_list|<
name|TestAvlNode
argument_list|>
argument_list|()
block|{
specifier|private
name|int
name|prevKey
init|=
operator|-
literal|1
decl_stmt|;
annotation|@
name|Override
specifier|public
name|boolean
name|visitNode
parameter_list|(
name|TestAvlNode
name|node
parameter_list|)
block|{
name|assertEquals
argument_list|(
name|prevKey
argument_list|,
name|node
operator|.
name|getKey
argument_list|()
operator|-
literal|1
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|node
operator|.
name|getKey
argument_list|()
operator|>=
name|MIN_KEY
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|node
operator|.
name|getKey
argument_list|()
operator|<=
name|MAX_KEY
argument_list|)
expr_stmt|;
name|prevKey
operator|=
name|node
operator|.
name|getKey
argument_list|()
expr_stmt|;
return|return
name|node
operator|.
name|getKey
argument_list|()
operator|<=
name|MAX_KEY
return|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testAvlTreeIterSeekFirst
parameter_list|()
block|{
specifier|final
name|int
name|MIN_KEY
init|=
literal|1
decl_stmt|;
specifier|final
name|int
name|MAX_KEY
init|=
literal|50
decl_stmt|;
name|TestAvlNode
name|root
init|=
literal|null
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
name|MIN_KEY
init|;
name|i
operator|<
name|MAX_KEY
condition|;
operator|++
name|i
control|)
block|{
name|root
operator|=
name|AvlTree
operator|.
name|insert
argument_list|(
name|root
argument_list|,
operator|new
name|TestAvlNode
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|AvlTreeIterator
argument_list|<
name|TestAvlNode
argument_list|>
name|iter
init|=
operator|new
name|AvlTreeIterator
argument_list|<>
argument_list|(
name|root
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|iter
operator|.
name|hasNext
argument_list|()
argument_list|)
expr_stmt|;
name|long
name|prevKey
init|=
literal|0
decl_stmt|;
while|while
condition|(
name|iter
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|TestAvlNode
name|node
init|=
name|iter
operator|.
name|next
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|prevKey
operator|+
literal|1
argument_list|,
name|node
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
name|prevKey
operator|=
name|node
operator|.
name|getKey
argument_list|()
expr_stmt|;
block|}
name|assertEquals
argument_list|(
name|MAX_KEY
operator|-
literal|1
argument_list|,
name|prevKey
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testAvlTreeIterSeekTo
parameter_list|()
block|{
specifier|final
name|int
name|MIN_KEY
init|=
literal|1
decl_stmt|;
specifier|final
name|int
name|MAX_KEY
init|=
literal|50
decl_stmt|;
name|TestAvlNode
name|root
init|=
literal|null
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
name|MIN_KEY
init|;
name|i
operator|<
name|MAX_KEY
condition|;
name|i
operator|+=
literal|2
control|)
block|{
name|root
operator|=
name|AvlTree
operator|.
name|insert
argument_list|(
name|root
argument_list|,
operator|new
name|TestAvlNode
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|int
name|i
init|=
name|MIN_KEY
operator|-
literal|1
init|;
name|i
operator|<
name|MAX_KEY
operator|+
literal|1
condition|;
operator|++
name|i
control|)
block|{
name|AvlTreeIterator
argument_list|<
name|TestAvlNode
argument_list|>
name|iter
init|=
operator|new
name|AvlTreeIterator
argument_list|<>
argument_list|(
name|root
argument_list|,
name|i
argument_list|,
name|KEY_COMPARATOR
argument_list|)
decl_stmt|;
if|if
condition|(
name|i
operator|<
name|MAX_KEY
condition|)
block|{
name|assertTrue
argument_list|(
name|iter
operator|.
name|hasNext
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// searching for something greater than the last node
name|assertFalse
argument_list|(
name|iter
operator|.
name|hasNext
argument_list|()
argument_list|)
expr_stmt|;
break|break;
block|}
name|TestAvlNode
name|node
init|=
name|iter
operator|.
name|next
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
operator|(
name|i
operator|%
literal|2
operator|==
literal|0
operator|)
condition|?
name|i
operator|+
literal|1
else|:
name|i
argument_list|,
name|node
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
name|long
name|prevKey
init|=
name|node
operator|.
name|getKey
argument_list|()
decl_stmt|;
while|while
condition|(
name|iter
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|node
operator|=
name|iter
operator|.
name|next
argument_list|()
expr_stmt|;
name|assertTrue
argument_list|(
name|node
operator|.
name|getKey
argument_list|()
operator|>
name|prevKey
argument_list|)
expr_stmt|;
name|prevKey
operator|=
name|node
operator|.
name|getKey
argument_list|()
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testAvlIterableListCrud
parameter_list|()
block|{
specifier|final
name|int
name|NITEMS
init|=
literal|10
decl_stmt|;
name|TestLinkedAvlNode
name|prependHead
init|=
literal|null
decl_stmt|;
name|TestLinkedAvlNode
name|appendHead
init|=
literal|null
decl_stmt|;
comment|// prepend()/append()
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<=
name|NITEMS
condition|;
operator|++
name|i
control|)
block|{
name|TestLinkedAvlNode
name|pNode
init|=
operator|new
name|TestLinkedAvlNode
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|assertFalse
argument_list|(
name|AvlIterableList
operator|.
name|isLinked
argument_list|(
name|pNode
argument_list|)
argument_list|)
expr_stmt|;
name|prependHead
operator|=
name|AvlIterableList
operator|.
name|prepend
argument_list|(
name|prependHead
argument_list|,
name|pNode
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|AvlIterableList
operator|.
name|isLinked
argument_list|(
name|pNode
argument_list|)
argument_list|)
expr_stmt|;
name|TestLinkedAvlNode
name|aNode
init|=
operator|new
name|TestLinkedAvlNode
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|assertFalse
argument_list|(
name|AvlIterableList
operator|.
name|isLinked
argument_list|(
name|aNode
argument_list|)
argument_list|)
expr_stmt|;
name|appendHead
operator|=
name|AvlIterableList
operator|.
name|append
argument_list|(
name|appendHead
argument_list|,
name|aNode
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|AvlIterableList
operator|.
name|isLinked
argument_list|(
name|aNode
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// readNext()
name|TestLinkedAvlNode
name|pNode
init|=
name|prependHead
decl_stmt|;
name|TestLinkedAvlNode
name|aNode
init|=
name|appendHead
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<=
name|NITEMS
condition|;
operator|++
name|i
control|)
block|{
name|assertEquals
argument_list|(
name|NITEMS
operator|-
name|i
argument_list|,
name|pNode
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
name|pNode
operator|=
name|AvlIterableList
operator|.
name|readNext
argument_list|(
name|pNode
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|i
argument_list|,
name|aNode
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
name|aNode
operator|=
name|AvlIterableList
operator|.
name|readNext
argument_list|(
name|aNode
argument_list|)
expr_stmt|;
block|}
comment|// readPrev()
name|pNode
operator|=
name|AvlIterableList
operator|.
name|readPrev
argument_list|(
name|prependHead
argument_list|)
expr_stmt|;
name|aNode
operator|=
name|AvlIterableList
operator|.
name|readPrev
argument_list|(
name|appendHead
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<=
name|NITEMS
condition|;
operator|++
name|i
control|)
block|{
name|assertEquals
argument_list|(
name|i
argument_list|,
name|pNode
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
name|pNode
operator|=
name|AvlIterableList
operator|.
name|readPrev
argument_list|(
name|pNode
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|NITEMS
operator|-
name|i
argument_list|,
name|aNode
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
name|aNode
operator|=
name|AvlIterableList
operator|.
name|readPrev
argument_list|(
name|aNode
argument_list|)
expr_stmt|;
block|}
comment|// appendList()
name|TestLinkedAvlNode
name|node
init|=
name|AvlIterableList
operator|.
name|appendList
argument_list|(
name|prependHead
argument_list|,
name|appendHead
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
name|NITEMS
init|;
name|i
operator|>=
literal|0
condition|;
operator|--
name|i
control|)
block|{
name|assertEquals
argument_list|(
name|i
argument_list|,
name|node
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
name|node
operator|=
name|AvlIterableList
operator|.
name|readNext
argument_list|(
name|node
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<=
name|NITEMS
condition|;
operator|++
name|i
control|)
block|{
name|assertEquals
argument_list|(
name|i
argument_list|,
name|node
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
name|node
operator|=
name|AvlIterableList
operator|.
name|readNext
argument_list|(
name|node
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
specifier|static
class|class
name|TestAvlNode
extends|extends
name|AvlNode
argument_list|<
name|TestAvlNode
argument_list|>
block|{
specifier|private
specifier|final
name|int
name|key
decl_stmt|;
specifier|public
name|TestAvlNode
parameter_list|(
name|int
name|key
parameter_list|)
block|{
name|this
operator|.
name|key
operator|=
name|key
expr_stmt|;
block|}
specifier|public
name|int
name|getKey
parameter_list|()
block|{
return|return
name|key
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|compareTo
parameter_list|(
name|TestAvlNode
name|other
parameter_list|)
block|{
return|return
name|this
operator|.
name|key
operator|-
name|other
operator|.
name|key
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|String
operator|.
name|format
argument_list|(
literal|"TestAvlNode(%d)"
argument_list|,
name|key
argument_list|)
return|;
block|}
block|}
specifier|private
specifier|static
class|class
name|TestLinkedAvlNode
extends|extends
name|AvlLinkedNode
argument_list|<
name|TestLinkedAvlNode
argument_list|>
block|{
specifier|private
specifier|final
name|int
name|key
decl_stmt|;
specifier|public
name|TestLinkedAvlNode
parameter_list|(
name|int
name|key
parameter_list|)
block|{
name|this
operator|.
name|key
operator|=
name|key
expr_stmt|;
block|}
specifier|public
name|int
name|getKey
parameter_list|()
block|{
return|return
name|key
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|compareTo
parameter_list|(
name|TestLinkedAvlNode
name|other
parameter_list|)
block|{
return|return
name|this
operator|.
name|key
operator|-
name|other
operator|.
name|key
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|String
operator|.
name|format
argument_list|(
literal|"TestLinkedAvlNode(%d)"
argument_list|,
name|key
argument_list|)
return|;
block|}
block|}
specifier|private
specifier|static
class|class
name|TestAvlKeyComparator
implements|implements
name|AvlKeyComparator
argument_list|<
name|TestAvlNode
argument_list|>
block|{
annotation|@
name|Override
specifier|public
name|int
name|compareKey
parameter_list|(
name|TestAvlNode
name|node
parameter_list|,
name|Object
name|key
parameter_list|)
block|{
return|return
name|node
operator|.
name|getKey
argument_list|()
operator|-
operator|(
name|int
operator|)
name|key
return|;
block|}
block|}
block|}
end_class

end_unit

