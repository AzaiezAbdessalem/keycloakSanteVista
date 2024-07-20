import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-faq',
  templateUrl: './faq.component.html',
  styleUrls: ['./faq.component.css']
})
export class FaqComponent implements OnInit {

  items = [
    { title: 'Q : Quest-ce qu un régime équilibré ?', content: 'Un régime équilibré comprend une variété d aliments dans les bonnes proportions, garantissant que vous obtenez tous les nutriments essentiels dont votre corps a besoin. Cela inclut généralement des fruits, des légumes, des grains entiers, des protéines et des graisses saines.' },
    { title: 'Q : Combien d eau devrais-je boire par jour ?', content: 'La recommandation générale est de boire environ 8 verres (2 litres) d eau par jour, mais les besoins individuels peuvent varier en fonction de facteurs tels que le niveau d activité, le climat et l état de santé général.' },
    { title: 'Q : Comment la nutrition affecte-t-elle la santé mentale ?', content: ' Une bonne nutrition soutient la santé mentale en fournissant des nutriments essentiels qui influencent la fonction cérébrale, l humeur et les niveaux d énergie. Un régime équilibré peut aider à réduire le risque de problèmes de santé mentale tels que la dépression et l anxiété.' }
  ];


  activeIndex: number | null = null;

  constructor() { }

  ngOnInit(): void {
  }

  toggle(index: number) {
    if (this.activeIndex === index) {
      this.activeIndex = null;
    } else {
      this.activeIndex = index;
    }
  }

  isOpen(index: number): boolean {
    return this.activeIndex === index;
  }

}
